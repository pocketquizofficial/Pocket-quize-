/**
 * PocketQuiz PWA - Service Worker Engine
 * Keeps the application fast, performant, and loadable offline.
 */

const CACHE_NAME = "pocketquiz-pwa-v1";
const ASSETS_TO_CACHE = [
  "index.html",
  "style.css",
  "script.js",
  "manifest.json",
  "https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@700&display=swap",
  "https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"
];

// SW Install Event
self.addEventListener("install", event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => {
      console.log("[Service Worker] Caching app assets...");
      return cache.addAll(ASSETS_TO_CACHE);
    }).then(() => self.skipWaiting())
  );
});

// SW Activate Event
self.addEventListener("activate", event => {
  event.waitUntil(
    caches.keys().then(keys => {
      return Promise.all(
        keys.map(key => {
          if (key !== CACHE_NAME) {
            console.log("[Service Worker] Purging stale cache:", key);
            return caches.delete(key);
          }
        })
      );
    }).then(() => self.clients.claim())
  );
});

// SW Fetch Event (Network-First Fallback-to-Cache Strategy)
self.addEventListener("fetch", event => {
  // Only intercept HTTP/HTTPS resources
  if (!event.request.url.startsWith("http")) return;

  event.respondWith(
    fetch(event.request)
      .then(response => {
        // Clone and store active page updates
        const resClone = response.clone();
        caches.open(CACHE_NAME).then(cache => {
          cache.put(event.request, resClone);
        });
        return response;
      })
      .catch(() => {
        // Fallback to cache when internet connection is lost
        return caches.match(event.request).then(cachedResponse => {
          if (cachedResponse) {
            return cachedResponse;
          }
          // If the resource is not found in cache, return an offline template if it's a page navigation
          if (event.request.mode === "navigate") {
            return caches.match("index.html");
          }
        });
      })
  );
});
