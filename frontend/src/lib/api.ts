// In production, requests go through Next.js rewrites (no CORS).
// The rewrites in next.config.ts proxy:
//   /api/core/* → CORE_API (Java backend)
//   /api/rec/*  → REC_API  (Python recommendation service)
const CORE_API_PREFIX = "/api/core";
const REC_API_PREFIX = "/api/rec";

export function getAuthToken(): string | null {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('token');
  }
  return null;
}

export function setAuthToken(token: string) {
  if (typeof window !== 'undefined') {
    localStorage.setItem('token', token);
  }
}

export function removeAuthToken() {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('token');
  }
}

interface FetchOptions extends RequestInit {
  requireAuth?: boolean;
}

async function baseFetch(url: string, options: FetchOptions = {}) {
  const { requireAuth = false, headers, ...restOptions } = options;
  const reqHeaders = new Headers(headers);
  reqHeaders.set('Content-Type', 'application/json');
  reqHeaders.set('Accept', 'application/json');

  if (requireAuth) {
    const token = getAuthToken();
    if (token) {
      reqHeaders.set('Authorization', `Bearer ${token}`);
    } else {
      console.warn("Auth required but no token found in localStorage.");
    }
  }

  const response = await fetch(url, {
    headers: reqHeaders,
    ...restOptions,
  });

  if (!response.ok) {
    let errorMessage = `HTTP error! status: ${response.status}`;
    try {
      const errorBody = await response.json();
      errorMessage = errorBody.message || errorMessage;
    } catch (e) {
      // Ignore if parsing error body fails
    }
    throw new Error(errorMessage);
  }

  // Handle empty responses
  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return null;
  }

  return response.json();
}

/**
 * Fetch from Smarty Commerce Core Backend (Java)
 * Proxied through Next.js: /api/core/* → backend
 */
export async function fetchCoreApi(endpoint: string, options?: FetchOptions) {
  const url = `${CORE_API_PREFIX}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;
  return baseFetch(url, options);
}

/**
 * Fetch from Recommendation Service (Python)
 * Proxied through Next.js: /api/rec/* → recommendation service
 */
export async function fetchRecApi(endpoint: string, options?: FetchOptions) {
  const url = `${REC_API_PREFIX}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;
  return baseFetch(url, options);
}
