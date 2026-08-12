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

  function checkLoginToast() {
    var params = new URLSearchParams(window.location.search);
    if (params.has('loginExitoso') || params.get('login') === 'exitoso' || params.has('login_ok')) {
      mostrarToastLoginExitoso();
      var cleanParams = new URLSearchParams(window.location.search);
      cleanParams.delete('loginExitoso');
      cleanParams.delete('login');
      cleanParams.delete('login_ok');
      var searchStr = cleanParams.toString();
      var newUrl = window.location.pathname + (searchStr ? '?' + searchStr : '');
      window.history.replaceState({}, document.title, newUrl);
    }
  }

  function mostrarToastLoginExitoso() {
    var toast = document.createElement('div');
    toast.className = 'fm-toast-success';
    toast.innerHTML = '<div class="fm-toast-success-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg></div><span>¡Inicio de sesión exitoso!</span>';
    document.body.appendChild(toast);
    setTimeout(function() { toast.classList.add('show'); }, 50);
    setTimeout(function() {
      toast.classList.remove('show');
      setTimeout(function() {
        if (toast.parentNode) toast.parentNode.removeChild(toast);
      }, 400);
    }, 3500);
  }

  function initMobileSidebar() {
    var mobileMenuBtn = document.getElementById('fm-mobile-menu-btn');
    var sidebar = document.querySelector('.fm-sidebar');
    if (!mobileMenuBtn || !sidebar) return;

    var overlay = document.querySelector('.fm-sidebar-overlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.className = 'fm-sidebar-overlay';
      document.body.appendChild(overlay);
    }

    function toggleSidebar(e) {
      if (e) e.preventDefault();
      sidebar.classList.toggle('open');
      overlay.classList.toggle('show');
    }

    mobileMenuBtn.onclick = toggleSidebar;
    overlay.onclick = toggleSidebar;
  }

  function initGlobalHandlers() {
    checkLoginToast();
    initMobileSidebar();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initGlobalHandlers);
  } else {
    initGlobalHandlers();
  }

})();


