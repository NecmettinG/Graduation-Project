const CORE_API_URL = process.env.NEXT_PUBLIC_CORE_API_URL || 'http://localhost:8080/smarty-commerce';
const REC_API_URL = process.env.NEXT_PUBLIC_REC_API_URL || 'http://localhost:8000';

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
      // Optional: handle unauthorized redirection or throwing here
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
 */
export async function fetchCoreApi(endpoint: string, options?: FetchOptions) {
  const url = `${CORE_API_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;
  return baseFetch(url, options);
}

/**
 * Fetch from Recommendation Service (Python)
 */
export async function fetchRecApi(endpoint: string, options?: FetchOptions) {
  const url = `${REC_API_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;
  return baseFetch(url, options);
}
