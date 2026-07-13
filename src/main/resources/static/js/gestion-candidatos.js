/* ================================================
   GESTIÓN DE CANDIDATOS — REDISEÑO JS
   Match score SVG circular, tooltips, skeleton.
   ================================================ */

// ── STATE ───────────────────────────────────────

let currentPage = 0;
let currentSize = 10;
let currentSearch = '';
let currentEstado = '';
let selectedCandidatoId = null;

const CIRCLE_CIRCUMFERENCE = 2 * Math.PI * 37;

// ── DOM REFS ────────────────────────────────────

const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

const tableBody = $('#gc-table-body');
const pagination = $('#gc-pagination');
const statTotal = $('#gc-stat-total');
const statNuevos = $('#gc-stat-nuevos');
const statProceso = $('#gc-stat-proceso');
const statContratados = $('#gc-stat-contratados');

// ── SKELETON ────────────────────────────────────

function renderSkeletonRows(count) {
  const rows = [];
  for (let i = 0; i < count; i++) {
    rows.push(`
      <tr>
        <td><div class="gc-skeleton" style="width:36px;height:36px;border-radius:50%"></div></td>
        <td><div class="gc-skeleton" style="height:16px;width:60%"></div></td>
        <td><div class="gc-skeleton" style="height:16px;width:50%"></div></td>
        <td><div class="gc-skeleton" style="height:16px;width:40%"></div></td>
        <td><div class="gc-skeleton" style="height:20px;width:70px;border-radius:999px"></div></td>
        <td><div class="gc-skeleton" style="height:16px;width:30%"></div></td>
        <td><div class="gc-skeleton" style="height:16px;width:50%"></div></td>
      </tr>
    `);
  }
  return rows.join('');
}

// ── MATCH SCORE (circular SVG) ──────────────────

function renderMatchRing(score) {
  const pct = Math.round(score);
  const offset = CIRCLE_CIRCUMFERENCE - (pct / 100) * CIRCLE_CIRCUMFERENCE;
  return `
    <div class="gc-match-ring">
      <svg viewBox="0 0 80 80" width="80" height="80">
        <circle class="gc-match-ring-bg" cx="40" cy="40" r="37"/>
        <circle class="gc-match-ring-fill" cx="40" cy="40" r="37"
          stroke-dasharray="${CIRCLE_CIRCUMFERENCE}"
          stroke-dashoffset="${CIRCLE_CIRCUMFERENCE}"
          data-offset="${offset}"
        />
      </svg>
      <div class="gc-match-ring-text">
        <span class="gc-match-ring-pct">${pct}%</span>
        <span class="gc-match-ring-label">Match</span>
      </div>
    </div>
  `;
}

function animateMatchRings() {
  $$('.gc-match-ring-fill').forEach((circle) => {
    const target = parseFloat(circle.getAttribute('data-offset'));
    requestAnimationFrame(() => { circle.style.strokeDashoffset = target; });
  });
}

// ── AVATAR ──────────────────────────────────────

function getInitials(nombre, apellido) {
  const a = (nombre || ' ').charAt(0).toUpperCase();
  const b = (apellido || ' ').charAt(0).toUpperCase();
  return a + b;
}

// ── ESTADOS LABEL + BADGE ───────────────────────

const estadoMap = {
  REGISTRADO:     { label: 'Registrado',    badge: 'registered' },
  Disponible:     { label: 'Disponible',    badge: 'available' },
  'En proceso':   { label: 'En Proceso',    badge: 'progress' },
  Entrevista:     { label: 'Entrevista',    badge: 'interview' },
  Pendiente:      { label: 'Pendiente',     badge: 'pending' },
  Seleccionado:   { label: 'Seleccionado',  badge: 'hired' },
  Contratado:     { label: 'Contratado',    badge: 'hired' },
  Rechazado:      { label: 'Rechazado',     badge: 'rejected' },
  Descartado:     { label: 'Descartado',    badge: 'rejected' },
};

function getEstadoInfo(estado) {
  return estadoMap[estado] || { label: estado || 'Registrado', badge: 'registered' };
}

// ── RENDER TABLE ────────────────────────────────

function renderTable(data) {
  const items = data.data;
  if (!items || items.length === 0) {
    tableBody.innerHTML = `
      <tr>
        <td colspan="7">
          <div class="gc-table-empty">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.5">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <p style="margin:12px 0 0;font-weight:600;color:#94A3B8">No se encontraron candidatos</p>
            <span style="font-size:13px;color:#94A3B8">Intenta ajustar los filtros</span>
          </div>
        </td>
      </tr>
    `;
    return;
  }

  const rows = items.map((c) => {
    const ini = getInitials(c.nombre, c.apellido);
    const estado = getEstadoInfo(c.estado);
    const matchPct = c.matchScore != null ? Math.round(c.matchScore) : '—';
    return `
      <tr onclick="openDrawer(${c.id})" data-id="${c.id}">
        <td><div class="gc-avatar">${ini}</div></td>
        <td>
          <div class="gc-cell-name">
            <span class="gc-name">${c.nombre} ${c.apellido || ''}</span>
            <span class="gc-city">${c.ciudad || c.cargo || ''}</span>
          </div>
        </td>
        <td class="gc-cell-muted">${c.email}</td>
        <td class="gc-cell-muted">${c.telefono || '—'}</td>
        <td><span class="gc-badge gc-badge-${estado.badge}">${estado.label}</span></td>
        <td class="gc-cell-muted">${matchPct}%</td>
        <td>
          <div class="gc-actions">
            <button class="gc-action-btn" data-tooltip="Ver detalle" onclick="event.stopPropagation();openDrawer(${c.id})">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
            </button>
            <button class="gc-action-btn" data-tooltip="Cambiar estado" onclick="event.stopPropagation();openDrawer(${c.id})">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
            </button>
            <div class="gc-action-more">
              <button class="gc-action-btn" data-tooltip="Más acciones" onclick="event.stopPropagation();toggleDropdown(this)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>
              </button>
              <div class="gc-dropdown-menu">
                <button onclick="event.stopPropagation();openDrawer(${c.id})">✕ ${c.nombre} ${c.apellido || ''}</button>
                <button onclick="event.stopPropagation();cambiarEstado(${c.id},'Disponible')">✕ Marcar Disponible</button>
                <button onclick="event.stopPropagation();cambiarEstado(${c.id},'En proceso')">✕ Marcar En proceso</button>
              </div>
            </div>
          </div>
        </td>
      </tr>
    `;
  });
  tableBody.innerHTML = rows.join('');
}

// ── RENDER PAGINATION (estilo Admin) ────────────

function renderPagination(data) {
  const { currentPage: page, totalPages, totalElements, pageSize: size } = data;
  const from = page * size + 1;
  const to = Math.min((page + 1) * size, totalElements);

  // Rows per page
  let leftHtml = '<div style="display:flex;align-items:center;gap:6px;font-size:13px;color:#64748B">';
  leftHtml += '<select class="pf-select" onchange="changeSize(this.value)">';
  [5, 10, 20, 50].forEach((s) => {
    leftHtml += `<option value="${s}" ${size === s ? 'selected' : ''}>${s}</option>`;
  });
  leftHtml += '</select>';
  leftHtml += `<span class="gc-pagination-info">${from}–${to} de ${totalElements}</span>`;
  leftHtml += '</div>';

  // Page pills
  let navHtml = '';
  if (totalPages > 1) {
    navHtml = '<nav><ul class="pagination mb-0">';
    navHtml += `<li class="page-item ${page <= 0 ? 'disabled' : ''}">`;
    navHtml += `<a class="page-link" onclick="goPage(${page - 1})" style="cursor:pointer">&lsaquo;</a></li>`;

    const maxVisible = 5;
    let start = Math.max(0, page - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages, start + maxVisible);
    if (end - start < maxVisible) start = Math.max(0, end - maxVisible);

    if (start > 0) {
      navHtml += `<li class="page-item"><a class="page-link" onclick="goPage(0)" style="cursor:pointer">1</a></li>`;
      if (start > 1) navHtml += '<li class="page-item disabled"><span class="page-link">&hellip;</span></li>';
    }
    for (let i = start; i < end; i++) {
      navHtml += `<li class="page-item ${i === page ? 'active' : ''}">`;
      navHtml += `<a class="page-link" onclick="goPage(${i})" style="cursor:pointer">${i + 1}</a></li>`;
    }
    if (end < totalPages) {
      if (end < totalPages - 1) navHtml += '<li class="page-item disabled"><span class="page-link">&hellip;</span></li>';
      navHtml += `<li class="page-item"><a class="page-link" onclick="goPage(${totalPages - 1})" style="cursor:pointer">${totalPages}</a></li>`;
    }
    navHtml += `<li class="page-item ${page >= totalPages - 1 ? 'disabled' : ''}">`;
    navHtml += `<a class="page-link" onclick="goPage(${page + 1})" style="cursor:pointer">&rsaquo;</a></li>`;
    navHtml += '</ul></nav>';
  }

  // Go to page
  const rightHtml = totalPages > 1 ? `
    <div class="gc-pagination-right">
      Ir a p&aacute;gina
      <input type="number" class="pf-goto-input" min="1" max="${totalPages}" value="${page+1}"
        onkeydown="if(event.key==='Enter'){const v=parseInt(this.value);if(v>=1&&v<=${totalPages})goPage(v-1);}"
      >
    </div>
  ` : '';

  pagination.innerHTML = `
    <div style="display:flex;align-items:center;gap:8px">${leftHtml}</div>
    ${navHtml}
    ${rightHtml}
  `;
}

// ── RENDER STATS ───────────────────────────────

function renderStats(stats) {
  statTotal.textContent = stats.total ?? 0;
  statNuevos.textContent = stats.nuevos ?? 0;
  statProceso.textContent = stats.enProceso ?? 0;
  statContratados.textContent = stats.contratados ?? 0;
}

// ── MONTH NAMES ─────────────────────────────

const MONTHS = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];

function monthName(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return MONTHS[d.getMonth()] || '';
}

function dayNum(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.getDate();
}

// ── RENDER DRAWER (tabs) ─────────────────────

function renderDrawer(c) {
  const drawer = $('#gc-drawer');
  const estado = getEstadoInfo(c.estado);
  const ini = getInitials(c.nombre, c.apellido);
  const matchHtml = renderMatchRing(c.matchScore || 0);
  const email = c.email || '';
  const tel = c.telefono || '';
  const linkedin = c.linkedin || '';

  drawer.innerHTML = `
    <div class="gc-drawer-content">
      <div class="gc-drawer-header">
        <div class="gc-drawer-avatar">${ini}</div>
        <div class="gc-drawer-title">
          <h3>${c.nombre}</h3>
          <span class="gc-drawer-role">${c.cargo || '—'}</span>
          <span class="gc-drawer-location">${c.ciudad || ''}</span>
        </div>
        <button class="gc-drawer-close" onclick="closeDrawer()">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
        </button>
      </div>

      <div class="gc-quick-actions">
        <a class="gc-quick-action-btn" href="mailto:${email}" title="Enviar correo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M22 7l-10 7L2 7"/></svg>
          Correo
        </a>
        <a class="gc-quick-action-btn" href="tel:${tel}" title="Llamar">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
          Llamar
        </a>
        <a class="gc-quick-action-btn" href="${linkedin || '#'}" target="_blank" title="LinkedIn" ${!linkedin ? 'style="opacity:0.4;pointer-events:none"' : ''}>
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z"/><rect x="2" y="9" width="4" height="12"/><circle cx="4" cy="4" r="2"/></svg>
          LinkedIn
        </a>
        <button class="gc-quick-action-btn" onclick="abrirPdfCV(${c.id})" title="Descargar CV">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
          CV
        </button>
      </div>

      <div class="gc-drawer-tabs">
        <button class="gc-tab active" onclick="switchTab('info', ${c.id})" data-tab="info">Info</button>
        <button class="gc-tab" onclick="switchTab('docs', ${c.id})" data-tab="docs">Docs</button>
        <button class="gc-tab" onclick="switchTab('entrevistas', ${c.id})" data-tab="entrevistas">Entrevistas</button>
        <button class="gc-tab" onclick="switchTab('notas', ${c.id})" data-tab="notas">Notas</button>
        <button class="gc-tab" onclick="switchTab('actividad', ${c.id})" data-tab="actividad">Actividad</button>
      </div>

      <div class="gc-tab-panel active" id="tab-info">
        <div class="gc-match-card" style="margin:0 0 12px">
          ${matchHtml}
          <div class="gc-match-info">
            <p class="gc-match-title">${c.cargo || 'Perfil profesional'}</p>
            <p class="gc-match-desc">${c.matchLabel || ''}</p>
            <p class="gc-match-sub">${c.experiencia || 0} años de experiencia</p>
          </div>
        </div>
        <div class="gc-info-grid">
          <div class="gc-info-row">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M22 7l-10 7L2 7"/></svg>
            <span class="gc-info-label">Correo</span>
            <span class="gc-info-value">${email}</span>
          </div>
          <div class="gc-info-row">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
            <span class="gc-info-label">Teléfono</span>
            <span class="gc-info-value">${tel || '—'}</span>
          </div>
          <div class="gc-info-row">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="10" r="3"/><path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"/></svg>
            <span class="gc-info-label">Ciudad</span>
            <span class="gc-info-value">${c.ciudad || '—'}</span>
          </div>
          <div class="gc-info-row">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c0 1.1 2.7 3 6 3s6-1.9 6-3v-5"/></svg>
            <span class="gc-info-label">Experiencia</span>
            <span class="gc-info-value">${c.experiencia || 0} años</span>
          </div>
          <div class="gc-info-row">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2a7 7 0 0 0-7 7c0 5.25 7 13 7 13s7-7.75 7-13a7 7 0 0 0-7-7z"/></svg>
            <span class="gc-info-label">Disponibilidad</span>
            <span class="gc-info-value">${c.disponibilidad || '—'}</span>
          </div>
          <div class="gc-info-row">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            <span class="gc-info-label">Estado</span>
            <span class="gc-info-value"><span class="gc-badge gc-badge-${estado.badge}">${estado.label}</span></span>
          </div>
          <div class="gc-info-row">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            <span class="gc-info-label">Proceso</span>
            <span class="gc-info-value">${c.procesoActual || '—'}</span>
          </div>
        </div>
        <div class="gc-info-actions">
          <button class="btn btn-primary btn-sm" onclick="abrirPdfCV(${c.id})">Ver CV completo</button>
          <select class="gc-filter-select" onchange="cambiarEstado(${c.id}, this.value)" style="width:100%">
            ${['REGISTRADO','Disponible','En proceso','Entrevista','Seleccionado','Contratado','Rechazado'].map(e =>
              `<option value="${e}" ${c.estado === e ? 'selected' : ''}>${e}</option>`
            ).join('')}
          </select>
        </div>
      </div>

      <div class="gc-tab-panel" id="tab-docs">
        <div class="gc-docs-list" id="gc-docs-list">
          <div class="gc-event-no-data">Cargando documentos...</div>
        </div>
        <div class="gc-drag-area" id="gc-drag-area" ondragover="event.preventDefault();this.classList.add('drag-over')" ondragleave="this.classList.remove('drag-over')" ondrop="event.preventDefault();this.classList.remove('drag-over');subirDocumento(${c.id}, event.dataTransfer.files[0])" onclick="document.getElementById('gc-file-input').click()">
          <div class="gc-drag-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
          </div>
          <p class="gc-drag-title">Subir documento</p>
          <p class="gc-drag-text">Arrastra un archivo aquí o haz clic para seleccionarlo</p>
          <p class="gc-drag-hint">PDF, DOC, XLS, JPG — Max 10 MB</p>
          <input type="file" id="gc-file-input" style="display:none" onchange="subirDocumento(${c.id}, this.files[0])">
        </div>
        <a class="gc-drive-link-card" href="/drive?folder=Candidatos/${encodeURIComponent(email)}" target="_blank">
          <div class="gc-drive-link-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
          </div>
          <div class="gc-drive-link-info">
            <p class="gc-drive-link-title">Ver en Drive</p>
            <p class="gc-drive-link-desc">Administrar todos los documentos del candidato</p>
          </div>
          <svg class="gc-drive-link-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
        </a>
      </div>

      <div class="gc-tab-panel" id="tab-entrevistas">
        <div class="gc-event-list" id="gc-event-list">
          <div class="gc-event-no-data">Cargando entrevistas...</div>
        </div>
      </div>

      <div class="gc-tab-panel" id="tab-notas">
        <textarea class="gc-nota-textarea" id="gc-nota-input" placeholder="Escribe una nota sobre este candidato..."></textarea>
        <button class="btn btn-primary btn-sm gc-nota-btn" onclick="alert('Función de notas próximamente')">Guardar nota</button>
      </div>

      <div class="gc-tab-panel" id="tab-actividad">
        <div id="gc-actividad-list">
          <div class="gc-event-no-data">No hay actividad reciente</div>
        </div>
      </div>
    </div>
  `;

  requestAnimationFrame(() => animateMatchRings());
}

// ── SWITCH TAB ──────────────────────────────

function switchTab(tab, candidatoId) {
  $$('.gc-tab').forEach(t => t.classList.remove('active'));
  $$('.gc-tab-panel').forEach(p => p.classList.remove('active'));
  const btn = document.querySelector(`.gc-tab[data-tab="${tab}"]`);
  if (btn) btn.classList.add('active');
  const panel = document.getElementById(`tab-${tab}`);
  if (panel) panel.classList.add('active');

  if (tab === 'docs') cargarDocumentos(candidatoId);
  if (tab === 'entrevistas') cargarEventos(candidatoId);
  if (tab === 'actividad') cargarActividad(candidatoId);
}

// ── CARGAR DOCUMENTOS ───────────────────────

function cargarDocumentos(id) {
  const list = $('#gc-docs-list');
  fetch(`/gestion-candidatos/${id}/documentos`, { credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } })
    .then(r => r.json())
    .then(docs => {
      if (!docs || docs.length === 0) {
        list.innerHTML = '<div class="gc-event-no-data">Sin documentos aún</div>';
        return;
      }
      list.innerHTML = docs.map(d => renderDocCard(d)).join('');
    })
    .catch(() => {
      list.innerHTML = '<div class="gc-event-no-data">Error al cargar documentos</div>';
    });
}

function renderDocCard(doc) {
  const ext = (doc.nombre || '').split('.').pop().toLowerCase();
  const iconMap = { pdf: '📄', doc: '📝', docx: '📝', xls: '📊', xlsx: '📊', jpg: '🖼', png: '🖼', jpeg: '🖼' };
  const icon = iconMap[ext] || '📄';
  const compartido = doc.estado === 'Compartido';

  return `
    <div class="gc-doc-card">
      <div class="gc-doc-icon">${icon}</div>
      <div class="gc-doc-info">
        <div class="gc-doc-name">${doc.nombre}</div>
        <div class="gc-doc-meta">
          <span class="gc-doc-status ${compartido ? 'gc-doc-status-compartido' : 'gc-doc-status-privado'}">
            ${compartido ? 'Compartido' : 'Privado'}
          </span>
        </div>
      </div>
      <div class="gc-doc-actions">
        <button class="gc-doc-action-btn" onclick="window.open('/drive/descargar?archivoId=${doc.id}','_blank')" title="Descargar">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
        </button>
        <button class="gc-doc-action-btn" onclick="window.open('/drive/ver-archivo/${doc.id}','_blank')" title="Ver">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
        </button>
      </div>
    </div>
  `;
}

// ── SUBIR DOCUMENTO ─────────────────────────

function subirDocumento(id, file) {
  if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  fetch(`/gestion-candidatos/${id}/documentos/subir`, {
    method: 'POST',
    credentials: 'same-origin',
    body: formData,
  })
    .then(r => r.json())
    .then(() => cargarDocumentos(id))
    .catch(err => alert('Error al subir: ' + err.message));
  document.getElementById('gc-file-input').value = '';
}

// ── CARGAR EVENTOS ──────────────────────────

function cargarEventos(id) {
  const list = $('#gc-event-list');
  fetch(`/gestion-candidatos/${id}/eventos`, { credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } })
    .then(r => r.json())
    .then(eventos => {
      if (!eventos || eventos.length === 0) {
        list.innerHTML = '<div class="gc-event-no-data">Sin entrevistas agendadas</div>';
        return;
      }
      list.innerHTML = eventos.map(e => `
        <div class="gc-event-card">
          <div class="gc-event-date-box">
            <span class="gc-event-day">${dayNum(e.fecha)}</span>
            <span class="gc-event-month">${monthName(e.fecha)}</span>
          </div>
          <div class="gc-event-info">
            <div class="gc-event-type">${e.tipo || 'Entrevista'}</div>
            <div class="gc-event-detail">
              <span>${e.fecha || ''}</span>
              <span>${e.hora || ''}</span>
              ${e.lugar ? `<span>📍 ${e.lugar}</span>` : ''}
            </div>
            ${e.observaciones ? `<div style="font-size:11px;color:#64748B;margin-top:2px">${e.observaciones}</div>` : ''}
          </div>
        </div>
      `).join('');
    })
    .catch(() => {
      list.innerHTML = '<div class="gc-event-no-data">Error al cargar entrevistas</div>';
    });
}

// ── CARGAR ACTIVIDAD ────────────────────────

function cargarActividad(id) {
  const list = $('#gc-actividad-list');
  fetch(`/gestion-candidatos/${id}`, { credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } })
    .then(r => r.json())
    .then(c => {
      const items = [];
      if (c.ultimaActualizacion) {
        items.push(`
          <div class="gc-actividad-item">
            <div class="gc-actividad-dot gc-actividad-dot-info"></div>
            <div>
              <div class="gc-actividad-text">Última actualización del perfil</div>
              <div class="gc-actividad-time">${c.ultimaActualizacion}</div>
            </div>
          </div>
        `);
      }
      if (c.estado) {
        items.push(`
          <div class="gc-actividad-item">
            <div class="gc-actividad-dot gc-actividad-dot-success"></div>
            <div>
              <div class="gc-actividad-text">Estado actual: ${c.estado}</div>
            </div>
          </div>
        `);
      }
      list.innerHTML = items.length > 0 ? items.join('') : '<div class="gc-event-no-data">No hay actividad reciente</div>';
    })
    .catch(() => {
      list.innerHTML = '<div class="gc-event-no-data">Error al cargar actividad</div>';
    });
}

// ── OPEN / CLOSE DRAWER ────────────────────────

function openDrawer(id) {
  selectedCandidatoId = id;
  const drawer = $('#gc-drawer');
  const grid = document.querySelector('.gc-main-grid');
  grid.classList.add('drawer-open');
  drawer.innerHTML = `
    <div class="gc-drawer-content">
      <div class="gc-drawer-header">
        <div class="gc-skeleton" style="width:52px;height:52px;border-radius:50%;flex-shrink:0"></div>
        <div style="flex:1"><div class="gc-skeleton" style="height:18px;width:60%;margin-bottom:6px"></div><div class="gc-skeleton" style="height:12px;width:40%"></div></div>
        <div class="gc-skeleton" style="width:28px;height:28px;border-radius:6px;flex-shrink:0"></div>
      </div>
      <div class="gc-skeleton" style="margin:16px;height:32px;border-radius:8px"></div>
      <div class="gc-skeleton" style="margin:0 16px;height:200px;border-radius:8px"></div>
    </div>
  `;

  fetch(`/gestion-candidatos/${id}`, { credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } })
    .then((r) => { if (!r.ok) throw new Error('Error al obtener candidato'); return r.json(); })
    .then((data) => {
      renderDrawer(data);
      $$('.gc-row-selected').forEach((el) => el.classList.remove('gc-row-selected'));
      const row = document.querySelector(`tr[data-id="${id}"]`);
      if (row) row.classList.add('gc-row-selected');
      cargarDocumentos(id);
    })
    .catch((err) => {
      console.error(err);
      drawer.innerHTML = '<div class="gc-drawer-empty"><p>Error al cargar</p><span>' + err.message + '</span></div>';
    });
}

function closeDrawer() {
  selectedCandidatoId = null;
  const drawer = $('#gc-drawer');
  const grid = document.querySelector('.gc-main-grid');
  grid.classList.remove('drawer-open');
  drawer.innerHTML = '';
  $$('.gc-row-selected').forEach((el) => el.classList.remove('gc-row-selected'));
}

// ── DROPDOWN ────────────────────────────────────

function toggleDropdown(btn) {
  $$('.gc-dropdown-menu.show').forEach((m) => { if (m.parentElement !== btn.parentElement) m.classList.remove('show'); });
  const menu = btn.parentElement.querySelector('.gc-dropdown-menu');
  if (menu) menu.classList.toggle('show');
}

document.addEventListener('click', (e) => {
  if (!e.target.closest('.gc-action-more')) {
    $$('.gc-dropdown-menu.show').forEach((m) => m.classList.remove('show'));
  }
});

// ── CAMBIAR ESTADO ──────────────────────────────

function cambiarEstado(id, nuevoEstado) {
  fetch(`/gestion-candidatos/${id}/estado`, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' },
    body: JSON.stringify({ estado: nuevoEstado }),
  })
    .then((r) => { if (!r.ok) throw new Error('Error al actualizar'); return r.json(); })
    .then(() => {
      loadPage(currentPage);
      if (selectedCandidatoId === id) openDrawer(id);
      loadStats();
    })
    .catch((err) => { console.error(err); alert('Error al cambiar estado: ' + err.message); });
}

// ── ABRIR CV ────────────────────────────────────

function abrirPdfCV(id) {
  window.open(`/gestion-candidatos/${id}/cv`, '_blank');
}

// ── NAVEGACIÓN ──────────────────────────────────

function goPage(page) {
  if (page < 0) return;
  currentPage = page;
  loadPage(page);
}

function changeSize(size) {
  currentSize = parseInt(size);
  currentPage = 0;
  loadPage(0);
}

// ── LOAD PAGE ───────────────────────────────────

function loadPage(page) {
  const params = new URLSearchParams();
  params.set('page', page);
  params.set('size', currentSize);
  if (currentSearch) params.set('search', currentSearch);
  if (currentEstado) params.set('estado', currentEstado);

  tableBody.innerHTML = renderSkeletonRows(currentSize);

  fetch(`/gestion-candidatos/api?${params.toString()}`, { credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } })
    .then((r) => { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
    .then((data) => {
      renderTable(data);
      renderPagination(data);
    })
    .catch((err) => {
      console.error('loadPage error:', err);
      tableBody.innerHTML = `<tr><td colspan="7"><div class="gc-table-empty"><p>Error al cargar datos</p><span>${err.message}</span></div></td></tr>`;
    });
}

// ── LOAD STATS ──────────────────────────────────

function loadStats() {
  fetch('/gestion-candidatos/stats', { credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } })
    .then((r) => r.json())
    .then((data) => renderStats(data))
    .catch((err) => console.error('Error loading stats:', err));
}

// ── FILTROS ─────────────────────────────────────

function filtrar() {
  currentSearch = $('#gc-search').value.trim();
  currentEstado = $('#gc-estado-filtro').value;
  currentPage = 0;
  loadPage(0);
}

// ── DEBOUNCE ────────────────────────────────────

let searchTimeout;
document.addEventListener('input', (e) => {
  if (e.target.id === 'gc-search') {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      currentSearch = e.target.value.trim();
      currentPage = 0;
      loadPage(0);
    }, 300);
  }
});

// ── LIMPIAR FILTROS ─────────────────────────────

function limpiarFiltros() {
  document.getElementById('gc-search').value = '';
  document.getElementById('gc-estado-filtro').value = '';
  currentSearch = '';
  currentEstado = '';
  currentPage = 0;
  loadPage(0);
}

// ── EXPORTAR ────────────────────────────────────

function exportarExcel() {
  const params = new URLSearchParams();
  if (currentSearch) params.set('search', currentSearch);
  if (currentEstado) params.set('estado', currentEstado);
  window.open(`/gestion-candidatos/export?${params.toString()}`, '_blank');
}

// ── SOLICITAR CARPETA (sidebar) ─────────────────

function solicitarNombreCarpeta() {
  const nombre = prompt('Nombre de la nueva carpeta:');
  if (nombre && nombre.trim()) {
    document.getElementById('inputNombreCarpetaGc').value = nombre.trim();
    document.getElementById('formCrearCarpetaGc').submit();
  }
}

// ── INIT ────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  loadStats();
  loadPage(0);
});
