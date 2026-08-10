(function() {
  function getCookie(name) {
    var match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? decodeURIComponent(match[2]) : null;
  }

  function getCsrfToken() {
    var cookieToken = getCookie('XSRF-TOKEN') || getCookie('CSRF-TOKEN');
    if (cookieToken) return cookieToken;

    var metaToken = document.querySelector('meta[name="_csrf"]');
    if (metaToken && metaToken.content) return metaToken.content;

    var inputToken = document.querySelector('input[name="_csrf"]');
    if (inputToken && inputToken.value) return inputToken.value;

    return null;
  }

  window.getCsrfToken = getCsrfToken;

  var originalFetch = window.fetch;
  window.fetch = function(url, options) {
    options = options || {};
    var method = (options.method || 'GET').toUpperCase();
    if (method !== 'GET' && method !== 'HEAD' && method !== 'OPTIONS') {
      var token = getCsrfToken();
      if (token) {
        if (!options.headers) {
          options.headers = {};
        }
        if (options.headers instanceof Headers) {
          if (!options.headers.has('X-XSRF-TOKEN')) options.headers.append('X-XSRF-TOKEN', token);
          if (!options.headers.has('X-CSRF-TOKEN')) options.headers.append('X-CSRF-TOKEN', token);
        } else if (typeof options.headers === 'object') {
          if (!options.headers['X-XSRF-TOKEN']) options.headers['X-XSRF-TOKEN'] = token;
          if (!options.headers['X-CSRF-TOKEN']) options.headers['X-CSRF-TOKEN'] = token;
        }
      }
    }
    return originalFetch.call(this, url, options);
  };

  var origOpen = XMLHttpRequest.prototype.open;
  XMLHttpRequest.prototype.open = function() {
    this._method = arguments[0];
    origOpen.apply(this, arguments);
  };
  var origSend = XMLHttpRequest.prototype.send;
  XMLHttpRequest.prototype.send = function() {
    var method = (this._method || 'GET').toUpperCase();
    if (method !== 'GET' && method !== 'HEAD' && method !== 'OPTIONS') {
      var token = getCsrfToken();
      if (token) {
        try {
          this.setRequestHeader('X-XSRF-TOKEN', token);
          this.setRequestHeader('X-CSRF-TOKEN', token);
        } catch(e) {}
      }
    }
    origSend.apply(this, arguments);
  };

  document.addEventListener('submit', function(event) {
    var form = event.target;
    if (form && form.tagName === 'FORM') {
      var method = (form.getAttribute('method') || 'GET').toUpperCase();
      if (method !== 'GET') {
        var existingCsrf = form.querySelector('input[name="_csrf"]');
        var token = getCsrfToken();
        if (token) {
          if (!existingCsrf) {
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = '_csrf';
            input.value = token;
            form.appendChild(input);
          } else if (!existingCsrf.value) {
            existingCsrf.value = token;
          }
        }
      }
    }
  }, true);

})();
