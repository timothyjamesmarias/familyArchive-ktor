/**
 * CSRF (Cross-Site Request Forgery) token utilities.
 *
 * Spring Security stores the CSRF token in a cookie named 'XSRF-TOKEN'
 * and expects it to be sent back in the 'X-XSRF-TOKEN' header for
 * POST/PUT/DELETE requests.
 */

/**
 * Get the CSRF token from cookies.
 *
 * @returns The CSRF token string, or null if not found
 */
export function getCsrfToken(): string | null {
  const name = 'XSRF-TOKEN';
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);

  if (parts.length === 2) {
    const token = parts.pop()?.split(';').shift();
    return token || null;
  }

  return null;
}

/**
 * Get headers object with CSRF token included.
 * Use this for all mutation requests (POST/PUT/DELETE).
 *
 * @param additionalHeaders - Optional additional headers to merge
 * @returns Headers object with CSRF token and any additional headers
 *
 * @example
 * ```typescript
 * fetch('/api/family-tree/individuals', {
 *   method: 'POST',
 *   headers: getCsrfHeaders({ 'Content-Type': 'application/json' }),
 *   body: JSON.stringify(data)
 * })
 * ```
 */
export function getCsrfHeaders(additionalHeaders?: Record<string, string>): Record<string, string> {
  const token = getCsrfToken();

  const headers: Record<string, string> = {
    ...additionalHeaders,
  };

  if (token) {
    headers['X-XSRF-TOKEN'] = token;
    console.log('[CSRF] Token found and added to headers:', token.substring(0, 10) + '...');
  } else {
    console.warn('[CSRF] No CSRF token found in cookies!');
    console.warn('[CSRF] Available cookies:', document.cookie);
  }

  return headers;
}

/**
 * Check if a CSRF token is available.
 * Useful for determining if mutations are allowed.
 *
 * @returns true if CSRF token exists, false otherwise
 */
export function hasCsrfToken(): boolean {
  return getCsrfToken() !== null;
}

/**
 * Wait for CSRF token to be available.
 * Useful on page load when the cookie might not be set yet.
 *
 * @param maxAttempts - Maximum number of polling attempts (default: 10)
 * @param interval - Milliseconds between attempts (default: 100)
 * @returns Promise that resolves when token is available or rejects on timeout
 */
export async function waitForCsrfToken(
  maxAttempts: number = 10,
  interval: number = 100
): Promise<string> {
  for (let i = 0; i < maxAttempts; i++) {
    const token = getCsrfToken();
    if (token) {
      return token;
    }
    await new Promise((resolve) => setTimeout(resolve, interval));
  }

  throw new Error('CSRF token not available after maximum attempts');
}
