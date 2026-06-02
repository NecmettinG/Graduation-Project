"""
End-to-End Test Script for the Recommendation Service

Prerequisites:
  - smarty-commerce must be running on http://localhost:8080
  - Your database must have products (you already have 461)
  - PostgreSQL must be running

This script will:
  1. Log in as admin to fetch existing product IDs
  2. Register 5 test users
  3. Create addresses and orders with overlapping products (to create co-purchase patterns)
  4. Add wishlist and cart items
  5. Start the recommendation service data fetch + matrix build
  6. Query recommendations and display results

Usage:
  cd recommendation-service
  .\venv\Scripts\python.exe tests/test_e2e.py
"""

import requests
import json
import random
import sys
import time

BASE_URL = "http://localhost:8080/smarty-commerce"
REC_URL = "http://localhost:8000"

# Admin credentials (from InitialUsersSetup.java)
ADMIN_EMAIL = "TheBigBoss@gmail.com"
ADMIN_PASSWORD = "Diamond Dawgs"

# Test users to create
TEST_USERS = [
    {"firstName": "Alice", "lastName": "TestUser", "email": "alice.test@example.com", "password": "Test1234!"},
    {"firstName": "Bob", "lastName": "TestUser", "email": "bob.test@example.com", "password": "Test1234!"},
    {"firstName": "Charlie", "lastName": "TestUser", "email": "charlie.test@example.com", "password": "Test1234!"},
    {"firstName": "Diana", "lastName": "TestUser", "email": "diana.test@example.com", "password": "Test1234!"},
    {"firstName": "Eve", "lastName": "TestUser", "email": "eve.test@example.com", "password": "Test1234!"},
]

HEADERS_JSON = {"Content-Type": "application/json", "Accept": "application/json"}


def print_step(step_num, description):
    print(f"\n{'='*60}")
    print(f"  STEP {step_num}: {description}")
    print(f"{'='*60}")


def print_success(msg):
    print(f"  ✅ {msg}")


def print_error(msg):
    print(f"  ❌ {msg}")


def print_info(msg):
    print(f"  ℹ️  {msg}")


# ──────────────────────────────────────────────
# STEP 1: Login as Admin
# ──────────────────────────────────────────────
def login(email, password):
    """Login and return (token, userId) tuple."""
    resp = requests.post(
        f"{BASE_URL}/users/login",
        json={"email": email, "password": password},
        headers=HEADERS_JSON,
    )
    if resp.status_code == 200:
        token = resp.headers.get("Authorization")
        user_id = resp.headers.get("UserId")
        return token, user_id
    else:
        print_error(f"Login failed for {email}: {resp.status_code} — {resp.text}")
        return None, None


# ──────────────────────────────────────────────
# STEP 2: Get product IDs from the database
# ──────────────────────────────────────────────
def get_product_ids(token, limit=50):
    """Fetch products and return list of productId strings."""
    resp = requests.get(
        f"{BASE_URL}/products?page=1&limit={limit}",
        headers={**HEADERS_JSON, "Authorization": token},
    )
    if resp.status_code == 200:
        products = resp.json()
        return [p["productId"] for p in products]
    else:
        print_error(f"Failed to fetch products: {resp.status_code}")
        return []


# ──────────────────────────────────────────────
# STEP 3: Register test users
# ──────────────────────────────────────────────
def register_user(user_data):
    """Register a user, return userId. Returns None if user already exists."""
    payload = {
        **user_data,
        "addresses": [
            {
                "city": "Istanbul",
                "country": "Turkey",
                "streetName": "Test Street 123",
                "postalCode": "34000",
                "type": "home",
            }
        ],
    }
    resp = requests.post(f"{BASE_URL}/users", json=payload, headers=HEADERS_JSON)
    if resp.status_code == 200:
        return resp.json().get("userId")
    else:
        # User might already exist — try logging in instead
        return None


# ──────────────────────────────────────────────
# STEP 4: Add items to cart
# ──────────────────────────────────────────────
def add_to_cart(token, user_id, product_id, quantity=1):
    resp = requests.post(
        f"{BASE_URL}/users/{user_id}/cart/items",
        json={"productId": product_id, "quantity": quantity},
        headers={**HEADERS_JSON, "Authorization": token},
    )
    return resp.status_code == 200


# ──────────────────────────────────────────────
# STEP 5: Create order (from cart)
# ──────────────────────────────────────────────
def get_user_addresses(token, user_id):
    resp = requests.get(
        f"{BASE_URL}/users/{user_id}/addresses",
        headers={**HEADERS_JSON, "Authorization": token},
    )
    if resp.status_code == 200:
        addrs = resp.json()
        if addrs:
            return addrs[0].get("addressId")
    return None


def create_order(token, user_id, address_id):
    resp = requests.post(
        f"{BASE_URL}/users/{user_id}/orders",
        json={
            "addressId": address_id,
            "paymentMethod": "UPON_DELIVERY",
        },
        headers={**HEADERS_JSON, "Authorization": token},
    )
    if resp.status_code == 201:
        return resp.json().get("orderId")
    else:
        print_error(f"  Order failed: {resp.status_code} — {resp.text[:200]}")
        return None


def clear_cart(token, user_id):
    requests.delete(
        f"{BASE_URL}/users/{user_id}/cart",
        headers={**HEADERS_JSON, "Authorization": token},
    )


# ──────────────────────────────────────────────
# STEP 6: Add to wishlist
# ──────────────────────────────────────────────
def add_to_wishlist(token, user_id, product_id):
    resp = requests.post(
        f"{BASE_URL}/users/{user_id}/wishlist/{product_id}",
        headers={**HEADERS_JSON, "Authorization": token},
    )
    return resp.status_code == 200


# ──────────────────────────────────────────────
# STEP 7: Test recommendation service
# ──────────────────────────────────────────────
def test_rec_health():
    try:
        resp = requests.get(f"{REC_URL}/health")
        return resp.status_code == 200
    except requests.ConnectionError:
        return False


def trigger_rebuild(token):
    resp = requests.post(
        f"{REC_URL}/admin/rebuild",
        headers={"Authorization": token},
    )
    return resp.status_code == 200, resp.json() if resp.status_code == 200 else resp.text


def get_rec_status(token):
    resp = requests.get(
        f"{REC_URL}/admin/status",
        headers={"Authorization": token},
    )
    return resp.json() if resp.status_code == 200 else None


def get_product_recs(token, product_id, limit=5):
    resp = requests.get(
        f"{BASE_URL}/products/{product_id}/recommendations?limit={limit}",
        headers={**HEADERS_JSON, "Authorization": token},
    )
    return resp.status_code, resp.json() if resp.status_code == 200 else resp.text


def get_user_recs(token, user_id, limit=5):
    resp = requests.get(
        f"{BASE_URL}/users/{user_id}/recommendations?limit={limit}",
        headers={**HEADERS_JSON, "Authorization": token},
    )
    return resp.status_code, resp.json() if resp.status_code == 200 else resp.text


# ──────────────────────────────────────────────
# MAIN TEST FLOW
# ──────────────────────────────────────────────
def main():
    print("\n" + "🔬 " * 20)
    print("  RECOMMENDATION SERVICE — END-TO-END TEST")
    print("🔬 " * 20)

    # ── Step 1: Check smarty-commerce is running ──
    print_step(1, "Checking smarty-commerce is running")
    try:
        requests.get(f"{BASE_URL}/products?page=1&limit=1", timeout=5)
        print_success("smarty-commerce is reachable")
    except requests.ConnectionError:
        print_error(f"Cannot connect to smarty-commerce at {BASE_URL}")
        print_info("Start it first: cd smarty-commerce/smarty-commerce && ./mvnw spring-boot:run")
        sys.exit(1)

    # ── Step 2: Login as admin ──
    print_step(2, "Logging in as admin")
    admin_token, admin_user_id = login(ADMIN_EMAIL, ADMIN_PASSWORD)
    if not admin_token:
        print_error("Could not log in as admin. Check credentials.")
        sys.exit(1)
    print_success(f"Admin logged in — userId: {admin_user_id}")

    # ── Step 3: Fetch product IDs ──
    print_step(3, "Fetching existing product IDs")
    all_product_ids = get_product_ids(admin_token, limit=50)
    if len(all_product_ids) < 10:
        print_error(f"Only found {len(all_product_ids)} products. Need at least 10.")
        sys.exit(1)
    print_success(f"Found {len(all_product_ids)} products (fetched first 50)")

    # Select product groups to create co-purchase patterns
    # Group A: products 0-4 (will be co-purchased by Alice, Bob, Charlie)
    # Group B: products 5-9 (will be co-purchased by Charlie, Diana, Eve)
    # This creates overlap at Charlie, making Group A & B products related
    group_a = all_product_ids[0:5]
    group_b = all_product_ids[5:10]
    extra_products = all_product_ids[10:15] if len(all_product_ids) > 14 else all_product_ids[5:10]

    print_info(f"Group A products (co-purchased): {group_a[:3]}...")
    print_info(f"Group B products (co-purchased): {group_b[:3]}...")

    # ── Step 4: Register test users ──
    print_step(4, "Registering test users")
    user_credentials = []  # List of (token, userId, email) tuples

    for user_data in TEST_USERS:
        user_id = register_user(user_data)
        if user_id:
            print_success(f"Registered {user_data['firstName']} — userId: {user_id}")
        else:
            print_info(f"{user_data['firstName']} may already exist, trying login...")

        token, user_id = login(user_data["email"], user_data["password"])
        if token:
            user_credentials.append((token, user_id, user_data["email"], user_data["firstName"]))
            print_success(f"Logged in as {user_data['firstName']} — userId: {user_id}")
        else:
            print_error(f"Could not authenticate {user_data['firstName']}")

    if len(user_credentials) < 3:
        print_error("Need at least 3 test users. Aborting.")
        sys.exit(1)

    # ── Step 5: Create co-purchase patterns via orders ──
    print_step(5, "Creating orders with co-purchase patterns")

    # Define which users buy which products
    # Alice buys Group A products
    # Bob buys Group A products (overlap with Alice!)
    # Charlie buys Group A + Group B (bridge user)
    # Diana buys Group B products
    # Eve buys Group B products (overlap with Diana!)
    purchase_plan = {
        0: group_a[:3],         # Alice: A0, A1, A2
        1: group_a[:4],         # Bob:   A0, A1, A2, A3
        2: group_a[2:] + group_b[:3],  # Charlie: A2, A3, A4, B0, B1, B2
        3: group_b[:4],         # Diana: B0, B1, B2, B3
        4: group_b[2:],         # Eve:   B2, B3, B4
    }

    for i, (token, user_id, email, name) in enumerate(user_credentials):
        if i not in purchase_plan:
            continue

        products_to_buy = purchase_plan[i]
        print_info(f"\n  {name} will order {len(products_to_buy)} products...")

        # Clear cart first
        clear_cart(token, user_id)

        # Get address
        address_id = get_user_addresses(token, user_id)
        if not address_id:
            print_error(f"  No address for {name}. Skipping order.")
            continue

        # Add items to cart one by one, then order
        for pid in products_to_buy:
            ok = add_to_cart(token, user_id, pid, quantity=1)
            if ok:
                print_success(f"  Added {pid[:10]}... to {name}'s cart")
            else:
                print_error(f"  Failed to add {pid[:10]}... to cart")

        # Place order
        order_id = create_order(token, user_id, address_id)
        if order_id:
            print_success(f"  ✓ {name}'s order created: {order_id[:15]}...")
        else:
            print_error(f"  Order failed for {name}")

    # ── Step 6: Add wishlist items ──
    print_step(6, "Adding wishlist items")

    # Alice wishlists some Group B products (cross-group interest)
    alice_token, alice_id = user_credentials[0][0], user_credentials[0][1]
    for pid in group_b[:2]:
        ok = add_to_wishlist(alice_token, alice_id, pid)
        if ok:
            print_success(f"Alice wishlisted {pid[:15]}...")

    # Eve wishlists some Group A products
    if len(user_credentials) >= 5:
        eve_token, eve_id = user_credentials[4][0], user_credentials[4][1]
        for pid in group_a[:2]:
            ok = add_to_wishlist(eve_token, eve_id, pid)
            if ok:
                print_success(f"Eve wishlisted {pid[:15]}...")

    # ── Step 7: Add cart items (without ordering) ──
    print_step(7, "Adding cart items (intent signals)")
    if len(user_credentials) >= 4:
        diana_token, diana_id = user_credentials[3][0], user_credentials[3][1]
        clear_cart(diana_token, diana_id)
        for pid in extra_products[:2]:
            add_to_cart(diana_token, diana_id, pid, 1)
            print_success(f"Diana has {pid[:15]}... in cart")

    # ── Step 8: Check recommendation service ──
    print_step(8, "Testing recommendation service")

    if not test_rec_health():
        print_error(f"Recommendation service is NOT running at {REC_URL}")
        print_info("Start it: cd recommendation-service && .\\venv\\Scripts\\python.exe main.py")
        print_info("\nOnce started, it will automatically fetch data and build the matrix.")
        print_info("Then re-run this script to test recommendations (skip to step 8).")
        print_info(f"\nOr manually trigger rebuild + test with these curl commands:")
        print_info(f"  curl {REC_URL}/health")
        print_info(f"  curl -X POST {REC_URL}/admin/rebuild -H \"Authorization: {admin_token}\"")
        print_info(f"  curl {REC_URL}/admin/status -H \"Authorization: {admin_token}\"")
        print_info(f"  curl \"{BASE_URL}/products/{group_a[0]}/recommendations?limit=5\" -H \"Authorization: {admin_token}\"")
        return

    print_success("Recommendation service is running!")

    # Trigger a manual rebuild
    print_info("Triggering manual matrix rebuild...")
    success, result = trigger_rebuild(admin_token)
    if success:
        print_success(f"Rebuild complete! Products: {result.get('productCount', '?')}, Users: {result.get('userCount', '?')}")
    else:
        print_error(f"Rebuild failed: {result}")
        return

    # Check status
    status = get_rec_status(admin_token)
    if status:
        print_info(f"Matrix status: {status['productCount']} products, {status['userCount']} users, density={status['matrixDensity']}")

    # ── Step 9: Get recommendations! ──
    print_step(9, "Fetching recommendations")

    # Product-to-product recommendations
    test_product = group_a[0]
    print_info(f"\n  Product recommendations for: {test_product}")
    code, recs = get_product_recs(admin_token, test_product, limit=5)
    if code == 200 and recs:
        print_success(f"  Got {len(recs)} product recommendations:")
        for r in recs:
            print(f"      → {r.get('productName', 'N/A')} (ID: {r.get('productId', '?')[:15]}...)")
    else:
        print_error(f"  Failed: {code} — {recs}")

    test_product_b = group_b[0]
    print_info(f"\n  Product recommendations for: {test_product_b}")
    code, recs = get_product_recs(admin_token, test_product_b, limit=5)
    if code == 200 and recs:
        print_success(f"  Got {len(recs)} product recommendations:")
        for r in recs:
            print(f"      → {r.get('productName', 'N/A')} (ID: {r.get('productId', '?')[:15]}...)")
    else:
        print_error(f"  Failed: {code} — {recs}")

    # User-based recommendations
    if user_credentials:
        test_user_token, test_user_id, _, test_user_name = user_credentials[0]
        print_info(f"\n  User recommendations for: {test_user_name} ({test_user_id})")
        code, recs = get_user_recs(test_user_token, test_user_id, limit=5)
        if code == 200 and recs:
            print_success(f"  Got {len(recs)} user recommendations:")
            for r in recs:
                print(f"      → {r.get('productName', 'N/A')} (ID: {r.get('productId', '?')[:15]}...)")
        else:
            print_error(f"  Failed: {code} — {recs}")

    # ── Summary ──
    print(f"\n{'='*60}")
    print("  ✅ END-TO-END TEST COMPLETE")
    print(f"{'='*60}")
    print(f"\n  You can also test interactively:")
    print(f"  • Swagger UI:    {BASE_URL}/swagger-ui.html")
    print(f"  • FastAPI Docs:  {REC_URL}/docs")
    print(f"  • Health Check:  {REC_URL}/health")
    print()


if __name__ == "__main__":
    main()
