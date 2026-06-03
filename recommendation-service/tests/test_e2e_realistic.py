"""
Realistic E2E Test Script — Recommendation Service

Creates 8 test users with realistic shopping personas that produce
meaningful co-purchase patterns from your 461-product catalog.

Personas:
  - TechEnthusiast (Ahmet)  : Phones + Computers + Peripherals
  - TechEnthusiast (Mehmet) : Phones + Computers + Wearables (overlaps with Ahmet)
  - Gamer (Kerem)           : Gaming Laptops + Consoles + Peripherals
  - Gamer (Emre)            : Consoles + Peripherals + Gaming Headsets (overlaps with Kerem)
  - Photographer (Ayse)     : Cameras + Computers
  - HomeEntertainment (Elif): TVs + Soundbars
  - Fashionista (Zeynep)    : Female Clothing across categories
  - Sporty (Can)            : Sports Wear + Fitness Equipment + Shoes

Expected results:
  - Recommend iPhone 16 Pro Max → other iPhones, Samsung flagships (co-purchased by tech users)
  - Recommend PS5 Pro → Xbox, Nintendo Switch (co-purchased by gamers)
  - Recommend Sony Alpha 7 IV → Canon R6, Fujifilm X-T5 (co-purchased by photographers)
  - User recs for Ahmet → products Mehmet bought but Ahmet didn't

Usage:
  1. Make sure smarty-commerce is running (localhost:8080)
  2. Run: .\\venv\\Scripts\\python.exe tests\\test_e2e_realistic.py
  3. Then start recommendation-service and trigger rebuild
"""

import requests
import sys
import time

BASE_URL = "http://localhost:8080/smarty-commerce"
REC_URL = "http://localhost:8000"

ADMIN_EMAIL = "TheBigBoss@gmail.com"
ADMIN_PASSWORD = "Diamond Dawgs"

HEADERS = {"Content-Type": "application/json", "Accept": "application/json"}


# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# SHOPPING PERSONAS — product IDs from your CSV
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

USERS = [
    {
        # Tech Enthusiast 1 — Apple + Samsung phones, MacBook, peripherals
        "firstName": "Ahmet", "lastName": "Yilmaz",
        "email": "ahmet.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: Phone + Watch (classic Apple combo)
            ["qxkXn9Xs778Efvv", "fM2lIpE8yw8Ls9f"],  # iPhone 16 Pro Max, Apple Watch Series 10
            # Order 2: Laptop + Peripheral
            ["n77crVSawIDa69e", "S2hb0hgvac8lRUN"],  # MacBook Air M3, Logitech MX Master 3S
        ],
        "wishlist": [
            "4MMhi0RbITCQHss",  # MacBook Pro 14" M3 Pro
            "hZxUP52Wli9pMoX",  # Apple Watch Ultra 2
        ],
        "cart": [
            "FlpZFKgffXK9CgV",  # SteelSeries Apex Pro TKL
        ],
    },
    {
        # Tech Enthusiast 2 — Samsung ecosystem + Laptop
        "firstName": "Mehmet", "lastName": "Kaya",
        "email": "mehmet.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: Samsung phone + watch (Samsung ecosystem)
            ["pfd0L1q5dGxcsb1", "WmH4m8yjROcSdhD"],  # Galaxy S25 Ultra, Galaxy Watch 7
            # Order 2: iPhone + MacBook (overlap with Ahmet!)
            ["qxkXn9Xs778Efvv", "n77crVSawIDa69e"],  # iPhone 16 Pro Max, MacBook Air M3
        ],
        "wishlist": [
            "eG0wI4GaYMGouok",  # Galaxy S25
            "6PLkIYSgjfvNly8",  # Samsung Galaxy Ring
        ],
        "cart": [
            "S2hb0hgvac8lRUN",  # Logitech MX Master 3S (same as Ahmet)
        ],
    },
    {
        # Gamer 1 — Console gaming + peripherals
        "firstName": "Kerem", "lastName": "Demir",
        "email": "kerem.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: PS5 + Gaming headset
            ["dMwxiKApGZZDDa6", "tK5pQrGB34CWATT"],  # PS5 Pro, HyperX Cloud III
            # Order 2: Gaming laptop + gaming mouse
            ["1cqqb9Zs26kPumZ", "kOIxjxaBDSzXPjj"],  # ASUS ROG Zephyrus G14, Razer DeathAdder V3
        ],
        "wishlist": [
            "HffpEdN1seT9qvl",  # Nintendo Switch 2
            "cVT6VmiPWoparGt",  # Steam Deck OLED
        ],
        "cart": [
            "2CrhcvmYmL0GgpD",  # Corsair K70 MAX
        ],
    },
    {
        # Gamer 2 — Console + PC gaming (overlaps with Kerem)
        "firstName": "Emre", "lastName": "Ozturk",
        "email": "emre.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: PS5 + Nintendo Switch (overlap with Kerem on PS5)
            ["dMwxiKApGZZDDa6", "HffpEdN1seT9qvl"],  # PS5 Pro, Nintendo Switch 2
            # Order 2: Gaming peripherals
            ["kOIxjxaBDSzXPjj", "LvT15PruSVhkavL", "2CrhcvmYmL0GgpD"],
            # Razer DeathAdder, Razer BlackShark, Corsair K70
        ],
        "wishlist": [
            "WX8aEE41i6bIIWB",  # Xbox Series X
            "TVnWXAil6QQHhMu",  # Wooting 60HE
        ],
        "cart": [
            "wtPw0YZU8657qPM",  # Elgato Stream Deck
        ],
    },
    {
        # Photographer — Cameras + MacBook for editing
        "firstName": "Ayse", "lastName": "Celik",
        "email": "ayse.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: Camera body
            ["YBv8PCFsLCongyH", "wHf2RpJiLdVzDzb"],  # Sony Alpha 7 IV, Fujifilm X-T5
            # Order 2: MacBook Pro for editing (overlap with tech users)
            ["4MMhi0RbITCQHss", "LOyyH7VdB7VP6cj"],  # MacBook Pro 14", DJI Osmo Action 4
        ],
        "wishlist": [
            "CghLleG9izHjBbZ",  # Canon EOS R6 Mark II
            "5i18Ua32bElkAz9",  # Nikon Z6 III
        ],
        "cart": [
            "GQZtg0grQfqsMfx",  # GoPro HERO12
        ],
    },
    {
        # Home Entertainment — TVs + Soundbars
        "firstName": "Elif", "lastName": "Sahin",
        "email": "elif.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: TV + Soundbar (classic combo)
            ["gv2wg6bcpTAkwSD", "1N5avyV7Z5aHQR0"],  # Samsung OLED TV, Sonos Arc
            # Order 2: Another TV
            ["YIbfaGRpnIwhfvC"],  # LG OLED TV
        ],
        "wishlist": [
            "o06y5yLzrmJ8Ko7",  # Sony OLED TV
            "qGAwkgokTIMGcdN",  # Bose Soundbar 900
        ],
        "cart": [
            "kBJnNE3XZpwxDsF",  # JBL Bar 1000 Pro
        ],
    },
    {
        # Fashionista — Female clothing across categories
        "firstName": "Zeynep", "lastName": "Arslan",
        "email": "zeynep.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: T-shirt + Shorts (summer outfit)
            ["dcTgZZCx5adZKu9", "NYhL7GUTYd7b2FJ"],  # Nike Women's T-Shirt, Nike Women's Shorts
            # Order 2: Dress + Jacket
            ["VKwpXKo3wbSASc3", "g6N5U1e31vG3BL8"],  # Zara Satin Slip Dress, Zara Faux Leather Biker Jacket
        ],
        "wishlist": [
            "CPyXTVLh6nhizLO",  # Adidas Women's Tee
            "npelPCurZqVPE2r",  # Levi's 501 Women's Shorts
        ],
        "cart": [
            "N1XeAWcIlAQGZzL",  # Zara Pleated Midi Skirt
        ],
    },
    {
        # Sporty — Fitness + Sports wear + Equipment
        "firstName": "Can", "lastName": "Aksoy",
        "email": "can.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: Sports wear
            ["XgpOy1ot9SemXiK", "eUzSnAjVI0k3Of9"],  # Nike M T-Shirt, Nike M Shorts
            # Order 2: Fitness equipment
            ["SMWmdrIStrtvnC1", "IYsPDDN0fLCzifB"],  # Cosfer Resistance Band, Lululemon Mat
        ],
        "wishlist": [
            "8UCG0OMEOJq4is7",  # Garmin Forerunner 265
            "hVn0WpRNxHhNvvP",  # Nike Training Duffel
        ],
        "cart": [
            "7nYwxJnWR05WgHu",  # Bowflex Bench
        ],
    },
    # ─── Overlap users (create cross-user signals for isolated categories) ───
    {
        # Home Entertainment 2 — overlaps with Elif on Samsung OLED TV
        "firstName": "Selin", "lastName": "Korkmaz",
        "email": "selin.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: Same Samsung TV as Elif + different soundbar
            ["gv2wg6bcpTAkwSD", "qGAwkgokTIMGcdN"],  # Samsung OLED TV, Bose Soundbar 900
            # Order 2: Sony TV + JBL Soundbar
            ["o06y5yLzrmJ8Ko7", "kBJnNE3XZpwxDsF"],  # Sony OLED TV, JBL Bar 1000 Pro
        ],
        "wishlist": [
            "Wx4IfNFOafz5yU8",  # Philips Ambilight TV
            "7NyOZhI6r7nmiCd",  # Sennheiser Ambeo Soundbar
        ],
        "cart": [
            "mIwalow0Cuf0rfY",  # TCL Mini-LED TV
        ],
    },
    {
        # Fashionista 2 — overlaps with Zeynep on Nike Women's T-Shirt + Zara Dress
        "firstName": "Defne", "lastName": "Yildiz",
        "email": "defne.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: Same Nike tee as Zeynep + Lululemon shorts
            ["dcTgZZCx5adZKu9", "SeVE3vPCW1Nh8Uo"],  # Nike Women's T-Shirt, Lululemon Align Shorts
            # Order 2: Same Zara dress as Zeynep + different skirt
            ["VKwpXKo3wbSASc3", "i43v4ke6Ti9lZ7Y"],  # Zara Satin Slip Dress, Levi's Iconic Midi Skirt
        ],
        "wishlist": [
            "jOlPJhoOaB1f52w",  # Reformation Juliette Dress
            "1gTTnnw4aBEQpZW",  # Lululemon Define Jacket
        ],
        "cart": [
            "Nd0GNebKIVRtGVR",  # Levi's Perfect Graphic Tee
        ],
    },
    {
        # Sporty 2 — overlaps with Can on Nike T-Shirt + Resistance Bands
        "firstName": "Baris", "lastName": "Cetin",
        "email": "baris.test@smarty.com", "password": "Test1234!",
        "orders": [
            # Order 1: Same Nike tee as Can + Adidas shorts
            ["XgpOy1ot9SemXiK", "Q8PyQS40DsnZASv"],  # Nike M T-Shirt, Adidas Squadra Shorts
            # Order 2: Same resistance bands as Can + foam roller
            ["SMWmdrIStrtvnC1", "e6YShLOaM6zsxgz"],  # Cosfer Resistance Band, TriggerPoint Foam Roller
        ],
        "wishlist": [
            "qJaGJcx2I1PYec3",  # Hydro Flask Water Bottle
            "i2mG15DR889MpzL",  # Crossrope Jump Rope
        ],
        "cart": [
            "i2xe2ZbeA56uPLp",  # Harbinger Weightlifting Gloves
        ],
    },
]


def log(icon, msg):
    print(f"  {icon} {msg}")


def login(email, password):
    resp = requests.post(f"{BASE_URL}/users/login", json={"email": email, "password": password}, headers=HEADERS)
    if resp.status_code == 200:
        return resp.headers.get("Authorization"), resp.headers.get("UserId")
    return None, None


def register_user(user):
    payload = {
        "firstName": user["firstName"], "lastName": user["lastName"],
        "email": user["email"], "password": user["password"],
        "addresses": [{"city": "Istanbul", "country": "Turkey", "streetName": "Test St", "postalCode": "34000", "type": "home"}],
    }
    resp = requests.post(f"{BASE_URL}/users", json=payload, headers=HEADERS)
    return resp.status_code == 200


def get_address_id(token, user_id):
    resp = requests.get(f"{BASE_URL}/users/{user_id}/addresses", headers={**HEADERS, "Authorization": token})
    if resp.status_code == 200 and resp.json():
        return resp.json()[0].get("addressId")
    return None


def add_to_cart(token, user_id, product_id):
    resp = requests.post(
        f"{BASE_URL}/users/{user_id}/cart/items",
        json={"productId": product_id, "quantity": 1},
        headers={**HEADERS, "Authorization": token},
    )
    return resp.status_code == 200


def clear_cart(token, user_id):
    requests.delete(f"{BASE_URL}/users/{user_id}/cart", headers={**HEADERS, "Authorization": token})


def create_order(token, user_id, address_id):
    resp = requests.post(
        f"{BASE_URL}/users/{user_id}/orders",
        json={"addressId": address_id, "paymentMethod": "UPON_DELIVERY"},
        headers={**HEADERS, "Authorization": token},
    )
    return resp.status_code == 201, resp.text


def add_to_wishlist(token, user_id, product_id):
    resp = requests.post(f"{BASE_URL}/users/{user_id}/wishlist/{product_id}", headers={**HEADERS, "Authorization": token})
    return resp.status_code == 200


def main():
    print("\n" + "=" * 60)
    print("  REALISTIC RECOMMENDATION TEST — 8 Personas, 461 Products")
    print("=" * 60)

    # Check backend
    try:
        requests.get(f"{BASE_URL}/products?page=1&limit=1", timeout=5)
        log("✅", "smarty-commerce is reachable")
    except:
        log("❌", f"Cannot reach {BASE_URL}. Start smarty-commerce first.")
        sys.exit(1)

    # Process each persona
    for user in USERS:
        print(f"\n{'─' * 50}")
        print(f"  👤 {user['firstName']} {user['lastName']} ({user['email']})")
        print(f"{'─' * 50}")

        # Register
        register_user(user)
        token, user_id = login(user["email"], user["password"])
        if not token:
            log("❌", "Login failed. Skipping.")
            continue
        log("✅", f"Authenticated — userId: {user_id}")

        # Get address
        address_id = get_address_id(token, user_id)
        if not address_id:
            log("❌", "No address found. Skipping orders.")
            continue

        # Save userId for summary
        user["userId"] = user_id
        user["token"] = token

        # Create orders
        for i, order_products in enumerate(user["orders"]):
            clear_cart(token, user_id)
            all_added = True
            for pid in order_products:
                if not add_to_cart(token, user_id, pid):
                    log("⚠️", f"Could not add {pid} to cart")
                    all_added = False

            if all_added:
                success, resp = create_order(token, user_id, address_id)
                if success:
                    log("🛒", f"Order {i + 1}: {len(order_products)} items ✓")
                else:
                    log("❌", f"Order {i + 1} failed: {resp[:100]}")
            else:
                log("⚠️", f"Order {i + 1} skipped (cart errors)")

        # Add wishlist items
        for pid in user.get("wishlist", []):
            if add_to_wishlist(token, user_id, pid):
                log("💜", f"Wishlisted: {pid[:15]}...")

        # Add cart items (leave them without ordering — intent signal)
        clear_cart(token, user_id)
        for pid in user.get("cart", []):
            if add_to_cart(token, user_id, pid):
                log("🛒", f"In cart (no order): {pid[:15]}...")

    # Summary
    print(f"\n{'=' * 60}")
    print("  ✅ INTERACTION DATA SEEDED SUCCESSFULLY!")
    print(f"{'=' * 60}")

    # Print userId table for Postman testing
    print("\n  User IDs for Postman testing:")
    print(f"  {'Name':<12} {'Email':<30} {'UserId'}")
    print(f"  {'─'*12} {'─'*30} {'─'*30}")
    for u in USERS:
        if "userId" in u:
            print(f"  {u['firstName']:<12} {u['email']:<30} {u['userId']}")

    print(f"""
  ─────────────────────────────────────────────────────
  Next steps:
  ─────────────────────────────────────────────────────

  1. Start recommendation-service (if not running):
     cd recommendation-service
     .\\venv\\Scripts\\python.exe main.py

  2. Trigger rebuild in Postman:
     POST http://localhost:8000/admin/rebuild
     Header: Authorization: <admin JWT token>

  3. Product Recommendations (Postman):

     iPhone 16 Pro Max → expect MacBook Air, Apple Watch, Galaxy S25 Ultra:
     GET http://localhost:8080/smarty-commerce/products/qxkXn9Xs778Efvv/recommendations?limit=5

     PS5 Pro → expect Nintendo Switch, Gaming peripherals:
     GET http://localhost:8080/smarty-commerce/products/dMwxiKApGZZDDa6/recommendations?limit=5

     Sony Alpha 7 IV → expect Fujifilm X-T5, MacBook Pro, DJI Action:
     GET http://localhost:8080/smarty-commerce/products/YBv8PCFsLCongyH/recommendations?limit=5

     Samsung OLED TV → expect Sonos Arc, LG OLED TV:
     GET http://localhost:8080/smarty-commerce/products/gv2wg6bcpTAkwSD/recommendations?limit=5

  4. User Recommendations (Postman):
     (Use each user's JWT token in the Authorization header)
""")
    for u in USERS:
        if "userId" in u:
            print(f"     {u['firstName']}: GET http://localhost:8080/smarty-commerce/users/{u['userId']}/recommendations?limit=5")
    print()


if __name__ == "__main__":
    main()
