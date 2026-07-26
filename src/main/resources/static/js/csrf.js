(function() {
  function getCookie(name) {
    var match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? decodeURIComponent(match[2]) : null;
  }
  var token = getCookie('XSRF-TOKEN');
  if (!token) return;

  var originalFetch = window.fetch;
  window.fetch = function(url, options) {
    if (!options) options = {};
    if (!options.headers) options.headers = {};
    if (!options.method || options.method.toUpperCase() !== 'GET') {
      options.headers['X-XSRF-TOKEN'] = token;
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
    if (this._method && this._method.toUpperCase() !== 'GET') {
      this.setRequestHeader('X-XSRF-TOKEN', token);
    }
    origSend.apply(this, arguments);
  };
})();
