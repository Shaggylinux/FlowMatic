function llenarModal(btn){
    document.getElementById("edit-id").value =
        btn.getAttribute("data-id");
    document.getElementById("edit-username").value =
        btn.getAttribute("data-username");
    document.getElementById("edit-apellido").value =
        btn.getAttribute("data-apellido");
    document.getElementById("edit-email").value =
        btn.getAttribute("data-email");
}

function obtenerQueryString() {
  var params = new URLSearchParams(window.location.search);
  var qs = '';
  var buscar = params.get('buscar');
  var rol = params.get('rol');
  if (buscar) qs += '&buscar=' + encodeURIComponent(buscar);
  if (rol) qs += '&rol=' + encodeURIComponent(rol);
  return qs;
}

function pfCambiarFilas(select){
    var base = select.getAttribute("data-base");
    if(base){
        window.location.href =
            base + "?page=0&size=" + select.value + obtenerQueryString();
    }
}

function pfIrAPagina(input){
    var p = parseInt(input.value);
    var max = parseInt(input.getAttribute("data-max"));
    if(!isNaN(p) && p>=1 && p<=max){
        var base = input.getAttribute("data-base");
        var size = input.getAttribute("data-size");
        window.location.href =
            base + "?page=" + (p-1) + "&size=" + size + obtenerQueryString();
    }
}

function filtrarRol(select) {
  var base = select.getAttribute("data-base") || '/admin';
  var buscar = new URLSearchParams(window.location.search).get('buscar') || '';
  var qs = '?page=0&size=10';
  if (buscar) qs += '&buscar=' + encodeURIComponent(buscar);
  if (select.value) qs += '&rol=' + encodeURIComponent(select.value);
  window.location.href = base + qs;
}

/* ── NOTIFICATIONS ──────────────────────────── */

function cargarNotificaciones() {
  const list = document.querySelector('#gc-notif-panel .gc-notif-list');
  const badge = document.getElementById('gc-notif-badge');
  if (!list) return;

  fetch('/notificaciones', { credentials: 'same-origin' })
    .then(r => { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
    .then(res => {
      const notifs = res.notificaciones || [];
      const total = res.total || 0;

      if (badge) {
        badge.textContent = total;
        badge.style.display = total > 0 ? 'flex' : 'none';
      }

      if (notifs.length === 0) {
        list.innerHTML = '<div class="gc-notif-empty">No hay notificaciones pendientes</div>';
        return;
      }

      list.innerHTML = notifs.map(n => `
        <div class="gc-notif-item ${n.leida ? 'gc-notif-leida' : 'gc-notif-no-leida'}" data-id="${n.id}">
          <div class="gc-notif-icon gc-notif-icon-${n.tipo.toLowerCase()}">
            ${n.tipo === 'ESTADO' ? '🔄' : n.tipo === 'EDICION' ? '✏️' : '📅'}
          </div>
          <div class="gc-notif-body">
            <div class="gc-notif-msg">${n.mensaje}</div>
            <div class="gc-notif-fecha">${n.fecha ? new Date(n.fecha).toLocaleDateString('es-CL', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }) : ''}</div>
          </div>
          <button class="gc-notif-marcar" onclick="marcarNotificacion(${n.id})" title="Marcar como leída">✓</button>
        </div>
      `).join('');
    })
    .catch(err => console.error('Error cargando notificaciones:', err));
}

function marcarNotificacion(id) {
  fetch('/notificaciones/' + id + '/leer', {
    method: 'POST',
    credentials: 'same-origin'
  })
    .then(r => r.json())
    .then(() => cargarNotificaciones())
    .catch(err => console.error('Error marcando notificación:', err));
}

function marcarTodasNotificaciones() {
  fetch('/notificaciones/leer-todas', {
    method: 'POST',
    credentials: 'same-origin'
  })
    .then(r => r.json())
    .then(() => cargarNotificaciones())
    .catch(err => console.error('Error marcando notificaciones:', err));
}

function toggleNotifPanel() {
  const panel = document.getElementById('gc-notif-panel');
  const btn = document.getElementById('gc-notif-btn');
  if (!panel || !btn) return;
  const isOpen = panel.classList.contains('gc-notif-open');
  panel.classList.toggle('gc-notif-open');
  btn.classList.toggle('gc-notif-btn-active');
  if (!isOpen) {
    cargarNotificaciones();
  }
}

document.addEventListener('click', function(e) {
  const panel = document.getElementById('gc-notif-panel');
  const btn = document.getElementById('gc-notif-btn');
  if (panel && btn && !panel.contains(e.target) && !btn.contains(e.target)) {
    panel.classList.remove('gc-notif-open');
    btn.classList.remove('gc-notif-btn-active');
  }
});

/* ── ACTIVITY LOG MODAL ────────────────────── */

var actPage = 0;
var actTotalPages = 0;
var actSize = 20;

var accionIconos = {
  CREACION: 'act-icon-creacion',
  EDICION: 'act-icon-edicion',
  ELIMINACION: 'act-icon-eliminacion',
  EXPORTACION: 'act-icon-exportacion',
  REGISTRO: 'act-icon-registro',
  SEGURIDAD: 'act-icon-seguridad'
};

var accionBadges = {
  CREACION: 'act-badge-creacion',
  EDICION: 'act-badge-edicion',
  ELIMINACION: 'act-badge-eliminacion',
  EXPORTACION: 'act-badge-exportacion',
  REGISTRO: 'act-badge-registro',
  SEGURIDAD: 'act-badge-seguridad'
};

var accionIconChar = {
  CREACION: '+',
  EDICION: '&#9998;',
  ELIMINACION: '&#10005;',
  EXPORTACION: '&#8593;',
  REGISTRO: '&#9997;',
  SEGURIDAD: '&#9888;'
};

function cargarActividad(page) {
  var list = document.getElementById('actividadList');
  var empty = document.getElementById('actividadEmpty');
  var loader = document.getElementById('actividadLoader');
  var footer = document.getElementById('actividadFooter');
  var btnPrev = document.getElementById('actBtnPrev');
  var btnNext = document.getElementById('actBtnNext');
  var pagInfo = document.getElementById('actPagInfo');

  if (!list) return;

  list.innerHTML = '';
  if (empty) empty.style.display = 'none';
  if (loader) loader.style.display = 'flex';
  if (footer) footer.style.display = 'none';

  fetch('/admin/actividad?page=' + page + '&size=' + actSize, { credentials: 'same-origin' })
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      actPage = res.page;
      actTotalPages = res.totalPages;
      var items = res.actividades || [];

      if (loader) loader.style.display = 'none';

      if (items.length === 0) {
        if (empty) empty.style.display = 'block';
        if (footer) footer.style.display = 'none';
        return;
      }

      if (footer) footer.style.display = 'flex';

      var html = '';
      for (var i = 0; i < items.length; i++) {
        var a = items[i];
        var iconClass = accionIconos[a.accion] || 'act-icon-creacion';
        var badgeClass = accionBadges[a.accion] || 'act-badge-creacion';
        var iconChar = accionIconChar[a.accion] || '+';
        var fecha = a.fecha ? formatearFechaRelativa(a.fecha) : '';
        var usuario = a.realizadoPor || 'Sistema';
        var badge = a.accion || '';

        html += '<div class="act-item">';
        html += '  <div class="act-icon ' + iconClass + '">' + iconChar + '</div>';
        html += '  <div class="act-body">';
        html += '    <div class="act-desc"><span class="act-badge ' + badgeClass + '">' + badge + '</span>' + escHtml(a.descripcion) + '</div>';
        html += '    <div class="act-meta"><span class="act-meta-user">' + escHtml(usuario) + '</span><span class="act-meta-dot"></span>' + fecha + '</div>';
        html += '  </div>';
        html += '</div>';
      }
      list.innerHTML = html;

      if (btnPrev) btnPrev.disabled = actPage <= 0;
      if (btnNext) btnNext.disabled = actPage >= actTotalPages - 1;
      if (pagInfo) pagInfo.textContent = 'P\u00e1gina ' + (actPage + 1) + ' de ' + actTotalPages;
    })
    .catch(function(err) {
      console.error('Error cargando actividad:', err);
      if (loader) loader.style.display = 'none';
      if (empty) {
        empty.textContent = 'Error al cargar la actividad';
        empty.style.display = 'block';
      }
    });
}

function formatearFechaRelativa(fechaStr) {
  try {
    var f = new Date(fechaStr);
    var ahora = new Date();
    var diffMs = ahora - f;
    var diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1) return 'Ahora';
    if (diffMin < 60) return 'Hace ' + diffMin + ' min';
    var diffHoras = Math.floor(diffMin / 60);
    if (diffHoras < 24) return 'Hace ' + diffHoras + ' h';
    var diffDias = Math.floor(diffHoras / 24);
    if (diffDias < 7) return 'Hace ' + diffDias + ' d\u00eda(s)';
    return f.toLocaleDateString('es-CL', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
  } catch(e) {
    return fechaStr;
  }
}

function escHtml(str) {
  if (!str) return '';
  var div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

/* ── AUDITORIA MODAL ───────────────────────── */

var audPage = 0;
var audTotalPages = 0;
var audSize = 20;

function cargarAuditoria(page) {
  var list = document.getElementById('auditoriaList');
  var empty = document.getElementById('auditoriaEmpty');
  var loader = document.getElementById('auditoriaLoader');
  var footer = document.getElementById('auditoriaFooter');
  var btnPrev = document.getElementById('audBtnPrev');
  var btnNext = document.getElementById('audBtnNext');
  var pagInfo = document.getElementById('audPagInfo');
  var badge = document.getElementById('audTotalBadge');

  if (!list) return;

  list.innerHTML = '';
  if (empty) empty.style.display = 'none';
  if (loader) loader.style.display = 'flex';
  if (footer) footer.style.display = 'none';

  fetch('/admin/auditoria?page=' + page + '&size=' + audSize, { credentials: 'same-origin' })
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      audPage = res.page;
      audTotalPages = res.totalPages;
      var items = res.eventos || [];
      var totalSeg = res.totalSeguridad || 0;

      if (loader) loader.style.display = 'none';
      if (badge) badge.textContent = totalSeg + ' evento(s) de seguridad';

      if (items.length === 0) {
        if (empty) empty.style.display = 'block';
        if (footer) footer.style.display = 'none';
        return;
      }

      if (footer) footer.style.display = 'flex';

      var html = '';
      for (var i = 0; i < items.length; i++) {
        var a = items[i];
        var fecha = a.fecha ? formatearFechaRelativa(a.fecha) : '';
        var usuario = a.realizadoPor || 'Sistema';
        var badgeText = a.accion || '';

        html += '<div class="aud-item">';
        html += '  <div class="aud-icon">&#9888;</div>';
        html += '  <div class="aud-body">';
        html += '    <div class="aud-desc">' + escHtml(a.descripcion) + '</div>';
        html += '    <div class="aud-meta"><span class="aud-meta-user">' + escHtml(usuario) + '</span><span class="aud-meta-dot"></span>' + fecha + '</div>';
        html += '  </div>';
        html += '</div>';
      }
      list.innerHTML = html;

      if (btnPrev) btnPrev.disabled = audPage <= 0;
      if (btnNext) btnNext.disabled = audPage >= audTotalPages - 1;
      if (pagInfo) pagInfo.textContent = 'P\u00e1gina ' + (audPage + 1) + ' de ' + audTotalPages;
    })
    .catch(function(err) {
      console.error('Error cargando auditoria:', err);
      if (loader) loader.style.display = 'none';
      if (empty) {
        empty.textContent = 'Error al cargar auditor\u00eda';
        empty.style.display = 'block';
      }
    });
}

/* ── CONFIGURACION MODAL ───────────────────── */

var etiquetasConfig = {
  'password.min.length': 'Longitud m\u00ednima de contrase\u00f1a',
  'login.max.attempts': 'Intentos m\u00e1ximos de inicio de sesi\u00f3n',
  'login.block.minutes': 'Minutos de bloqueo tras intentos',
  'password.reset.expiry.minutes': 'Caducidad del token de restablecimiento (min)',
  'app.name': 'Nombre de la aplicaci\u00f3n',
  'app.support.email': 'Correo de soporte'
};

function cargarConfiguraciones() {
  var list = document.getElementById('cfgSettingsList');
  if (!list) return;

  list.innerHTML = '<div class="cfg-loader">Cargando configuraci\u00f3n...</div>';

  fetch('/admin/configuraciones', { credentials: 'same-origin' })
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(configs) {
      var html = '<form action="/admin/configuraciones" method="post" class="cfg-settings-form">';
      for (var i = 0; i < configs.length; i++) {
        var c = configs[i];
        var label = etiquetasConfig[c.clave] || c.clave;
        var tipo = c.clave.indexOf('password') >= 0 ? 'password' : 'text';
        html += '<div class="cfg-form-group">';
        html += '  <label class="cfg-form-label">' + escHtml(label) + '</label>';
        html += '  <input type="' + tipo + '" name="cfg_' + c.clave + '" class="cfg-form-input" value="' + escHtml(c.valor) + '">';
        html += '</div>';
      }
      html += '<button type="submit" class="cfg-btn" style="margin-top:6px;">Guardar configuraci\u00f3n</button>';
      html += '</form>';
      list.innerHTML = html;
    })
    .catch(function(err) {
      console.error('Error cargando config:', err);
      list.innerHTML = '<div class="cfg-loader" style="color:#DC2626;">Error al cargar configuraci\u00f3n</div>';
    });
}

// Init modals on DOM ready
document.addEventListener('DOMContentLoaded', function() {
  var modalAct = document.getElementById('modalActividad');
  if (modalAct) {
    modalAct.addEventListener('show.bs.modal', function() {
      cargarActividad(0);
    });
  }

  var modalAud = document.getElementById('modalAuditoria');
  if (modalAud) {
    modalAud.addEventListener('show.bs.modal', function() {
      cargarAuditoria(0);
    });
  }

  var modalCfg = document.getElementById('modalConfiguracion');
  if (modalCfg) {
    modalCfg.addEventListener('show.bs.modal', function() {
      cargarConfiguraciones();
    });
  }

  cargarNotificaciones();
  setInterval(cargarNotificaciones, 30000);

  /* ── MOBILE SIDEBAR TOGGLE ────────────────────── */
  var mobileMenuBtn = document.getElementById('fm-mobile-menu-btn');
  var sidebar = document.querySelector('.fm-sidebar');
  
  if (mobileMenuBtn && sidebar) {
    var overlay = document.createElement('div');
    overlay.className = 'fm-sidebar-overlay';
    document.body.appendChild(overlay);

    function toggleSidebar() {
      sidebar.classList.toggle('open');
      overlay.classList.toggle('show');
    }

    mobileMenuBtn.addEventListener('click', toggleSidebar);
    overlay.addEventListener('click', toggleSidebar);
  }
});
