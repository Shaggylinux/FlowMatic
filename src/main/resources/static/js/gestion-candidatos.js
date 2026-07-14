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
        <td class="gc-cell-match"><div class="gc-match-track"><div class="gc-match-fill" style="width:${matchPct}%"></div></div><span class="gc-match-pct">${matchPct}%</span></td>
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

  const skills = (c.tecnologias || '').split(',').map(s => s.trim()).filter(Boolean);

  const etapas = ['Postulado','En revisión','Preseleccionado','Entrevista RRHH','Entrevista Técnica','Oferta','Contratado','Descartado'];

  drawer.innerHTML = `
    <div class="gc-drawer-content">
      <div class="gc-drawer-header">
        <div class="gc-drawer-avatar">${ini}</div>
        <div class="gc-drawer-title">
          <h3>${c.nombre}</h3>
          <span class="gc-drawer-role">${c.cargo || '—'}</span>
          <span class="gc-drawer-location">${c.ciudad || ''}</span>
          <span class="gc-drawer-meta"><span class="gc-badge gc-badge-${estado.badge}" style="font-size:10px;padding:2px 8px;">${estado.label}</span> <span style="color:#94A3B8;font-size:11px;">• ${c.ultimaActualizacion || '—'}</span></span>
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
        <button class="gc-quick-action-btn" onclick="abrirPdfCV(${c.id})" title="Descargar CV">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
          CV
        </button>
        <div class="gc-action-more" style="flex:1;">
          <button class="gc-quick-action-btn" onclick="event.stopPropagation();toggleDrawerDropdown(this)" style="width:100%;" title="Más acciones">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="5" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="19" r="1"/></svg>
            Más
          </button>
          <div class="gc-dropdown-menu" style="right:auto;left:0;">
            <button onclick="event.stopPropagation();closeDrawer();abrirModalEditar(${c.id})">Editar candidato</button>
            <button onclick="event.stopPropagation();closeDrawer();abrirModalEstado(${c.id}, '${c.nombre.replace(/'/g, "\\'")}')">Cambiar estado</button>
            <button onclick="event.stopPropagation();closeDrawer();window.location.href='/calendario'">Agendar entrevista</button>
            <button onclick="event.stopPropagation();closeDrawer();abrirModalCompartir(${c.id}, '${c.email}', '${c.nombre.replace(/'/g, "\\'")}')">Compartir expediente</button>
            <button onclick="event.stopPropagation();closeDrawer();abrirPdfCV(${c.id})">Descargar CV</button>
            <div class="gc-dropdown-divider"></div>
            <button onclick="event.stopPropagation();eliminarCandidato(${c.id}, '${c.nombre.replace(/'/g, "\\'")}')" style="color:#DC2626;">Eliminar candidato</button>
          </div>
        </div>
      </div>

      <div class="gc-drawer-tabs">
        <button class="gc-tab active" onclick="switchTab('info', ${c.id})" data-tab="info">Información</button>
        <button class="gc-tab" onclick="switchTab('expediente', ${c.id})" data-tab="expediente">Expediente</button>
        <button class="gc-tab" onclick="switchTab('entrevistas', ${c.id})" data-tab="entrevistas">Entrevistas</button>
        <button class="gc-tab" onclick="switchTab('notas', ${c.id})" data-tab="notas">Notas</button>
        <button class="gc-tab" onclick="switchTab('actividad', ${c.id})" data-tab="actividad">Actividad</button>
      </div>

      <div class="gc-tab-panel active" id="tab-info">
        <div class="gc-match-card" style="margin:0 0 16px">
          ${matchHtml}
          <div class="gc-match-info">
            <p class="gc-match-title">${c.cargo || 'Perfil profesional'}</p>
            <p class="gc-match-desc" style="font-weight:600;color:#0F172A;">${c.matchLabel || 'Coincidencia'}</p>
            <p class="gc-match-sub">${c.experiencia || 0} años de experiencia</p>
            ${skills.length > 0 ? '<div class="gc-drawer-chips" style="margin-top:6px;">' + skills.map(s => '<span class="gc-drawer-chip">' + s + '</span>').join('') + '</div>' : ''}
          </div>
        </div>

        <div class="gc-info-section">
          <div class="gc-info-section-title">Información Personal</div>
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
          </div>
        </div>

        <div class="gc-info-section">
          <div class="gc-info-section-title">Perfil Profesional</div>
          <div class="gc-info-grid">
            <div class="gc-info-row">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 7h-4.18A3 3 0 0 0 13 4h-2a3 3 0 0 0-2.82 3H4"/><rect x="2" y="7" width="20" height="14" rx="2"/></svg>
              <span class="gc-info-label">Cargo</span>
              <span class="gc-info-value">${c.cargo || '—'}</span>
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
          </div>
        </div>

        <div class="gc-info-section">
          <div class="gc-info-section-title">Proceso</div>
          <div class="gc-info-grid">
            <div class="gc-info-row">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
              <span class="gc-info-label">Estado</span>
              <span class="gc-info-value"><span class="gc-badge gc-badge-${estado.badge}">${estado.label}</span></span>
            </div>
            <div class="gc-info-row">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              <span class="gc-info-label">Etapa</span>
              <span class="gc-info-value">${c.procesoActual || '—'}</span>
            </div>
            <div class="gc-info-row">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              <span class="gc-info-label">Registro</span>
              <span class="gc-info-value">${c.ultimaActualizacion || '—'}</span>
            </div>
          </div>
        </div>

        <div class="gc-info-actions">
          <button class="btn btn-primary btn-sm" onclick="switchTab('expediente', ${c.id})" style="width:100%;">Abrir Expediente</button>
          <select class="gc-filter-select" onchange="cambiarEstado(${c.id}, this.value)" style="width:100%">
            ${etapas.map(e => `<option value="${e}" ${c.estado === e ? 'selected' : ''}>${e}</option>`).join('')}
          </select>
        </div>
      </div>

      <div class="gc-tab-panel" id="tab-expediente">
        <div class="gc-exp-section">
          <div class="gc-exp-section-title">Hoja de Vida</div>
          <div class="gc-docs-list" id="gc-docs-cv"><div class="gc-event-no-data">Sin documentos</div></div>
        </div>
        <div class="gc-exp-section">
          <div class="gc-exp-section-title">Certificados</div>
          <div class="gc-docs-list" id="gc-docs-cert"><div class="gc-event-no-data">Sin documentos</div></div>
        </div>
        <div class="gc-exp-section">
          <div class="gc-exp-section-title">Pruebas Técnicas</div>
          <div class="gc-docs-list" id="gc-docs-test"><div class="gc-event-no-data">Sin documentos</div></div>
        </div>
        <div class="gc-exp-section">
          <div class="gc-exp-section-title">Oferta Laboral</div>
          <div class="gc-docs-list" id="gc-docs-offer"><div class="gc-event-no-data">Sin documentos</div></div>
        </div>
        <div class="gc-exp-section">
          <div class="gc-exp-section-title">Contrato</div>
          <div class="gc-docs-list" id="gc-docs-contract"><div class="gc-event-no-data">Sin documentos</div></div>
        </div>
        <div class="gc-exp-section">
          <div class="gc-exp-section-title">Otros documentos</div>
          <div class="gc-docs-list" id="gc-docs-other"><div class="gc-event-no-data">Sin documentos</div></div>
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
            <p class="gc-drive-link-title">Abrir Drive</p>
            <p class="gc-drive-link-desc">Gestionar todos los documentos del candidato</p>
          </div>
          <svg class="gc-drive-link-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
        </a>
      </div>

      <div class="gc-tab-panel" id="tab-entrevistas">
        <div class="gc-timeline" id="gc-timeline">
          <div class="gc-event-no-data">Cargando entrevistas...</div>
        </div>
        <button class="btn btn-primary btn-sm" onclick="alert('Nueva entrevista — próximo')" style="width:100%;margin-top:12px;">+ Nueva Entrevista</button>
      </div>

      <div class="gc-tab-panel" id="tab-notas">
        <div class="gc-notas-list" id="gc-notas-list">
          <div class="gc-event-no-data">Sin notas aún</div>
        </div>
        <div style="margin-top:16px;border-top:1px solid #F1F5F9;padding-top:16px;">
          <textarea class="gc-nota-textarea" id="gc-nota-input" placeholder="Escribe una observación..."></textarea>
          <button class="btn btn-primary btn-sm gc-nota-btn" onclick="agregarNota(${c.id})">Agregar nota</button>
        </div>
      </div>

      <div class="gc-tab-panel" id="tab-actividad">
        <div class="gc-timeline" id="gc-timeline-actividad">
          <div class="gc-event-no-data">No hay actividad reciente</div>
        </div>
      </div>
    </div>
  `;

  requestAnimationFrame(() => animateMatchRings());
}

function toggleDrawerDropdown(btn) {
  $$('.gc-dropdown-menu.show').forEach(m => { if (m.parentElement !== btn.parentElement) m.classList.remove('show'); });
  const menu = btn.parentElement.querySelector('.gc-dropdown-menu');
  if (menu) menu.classList.toggle('show');
}

document.addEventListener('click', function(e) {
  if (!e.target.closest('.gc-action-more')) {
    $$('.gc-dropdown-menu.show').forEach(m => m.classList.remove('show'));
  }
});

// Cierra el dropdown automáticamente al hacer clic en cualquier botón dentro
document.addEventListener('click', function(e) {
  const btn = e.target.closest('.gc-dropdown-menu button, .gc-dropdown-menu a');
  if (btn) {
    btn.closest('.gc-dropdown-menu').classList.remove('show');
  }
}, true); // capturing phase para ejecutarse antes que event.stopPropagation

// ── SWITCH TAB ──────────────────────────────

function switchTab(tab, candidatoId) {
  $$('.gc-tab').forEach(t => t.classList.remove('active'));
  $$('.gc-tab-panel').forEach(p => p.classList.remove('active'));
  const btn = document.querySelector(`.gc-tab[data-tab="${tab}"]`);
  if (btn) btn.classList.add('active');
  const panel = document.getElementById(`tab-${tab}`);
  if (panel) panel.classList.add('active');

  if (tab === 'expediente') cargarDocumentos(candidatoId);
  if (tab === 'entrevistas') cargarEventos(candidatoId);
  if (tab === 'notas') cargarNotas(candidatoId);
  if (tab === 'actividad') cargarActividad(candidatoId);
}

// ── CARGAR DOCUMENTOS ───────────────────────

function cargarDocumentos(id) {
  const list = $('#gc-docs-other');
  if (!list) return;
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
  const list = $('#gc-timeline');
  fetch(`/gestion-candidatos/${id}/eventos`, { credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } })
    .then(r => r.json())
    .then(eventos => {
      if (!eventos || eventos.length === 0) {
        list.innerHTML = '<div class="gc-event-no-data">Sin entrevistas agendadas</div>';
        return;
      }
      list.innerHTML = eventos.map((e, i) => `
        <div class="gc-timeline-item ${i === eventos.length - 1 ? 'last' : ''}">
          <div class="gc-timeline-line"></div>
          <div class="gc-timeline-dot" style="background:${e.color || '#6366F1'};"></div>
          <div class="gc-timeline-content">
            <div class="gc-timeline-title">${e.tipo || 'Entrevista'}</div>
            <div class="gc-timeline-date">${e.fecha || ''} ${e.hora || ''}</div>
            ${e.lugar ? `<div class="gc-timeline-detail">📍 ${e.lugar}</div>` : ''}
            ${e.observaciones ? `<div class="gc-timeline-detail">${e.observaciones}</div>` : ''}
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
  const list = $('#gc-timeline-actividad');
  fetch(`/gestion-candidatos/${id}`, { credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } })
    .then(r => r.json())
    .then(c => {
      const items = [];
      if (c.ultimaActualizacion) {
        items.push(`
          <div class="gc-timeline-item last">
            <div class="gc-timeline-line"></div>
            <div class="gc-timeline-dot" style="background:#6366F1;"></div>
            <div class="gc-timeline-content">
              <div class="gc-timeline-title">Última actualización del perfil</div>
              <div class="gc-timeline-date">${c.ultimaActualizacion}</div>
            </div>
          </div>
        `);
      }
      if (c.estado) {
        items.push(`
          <div class="gc-timeline-item last">
            <div class="gc-timeline-line"></div>
            <div class="gc-timeline-dot" style="background:#22C55E;"></div>
            <div class="gc-timeline-content">
              <div class="gc-timeline-title">Estado actual: ${c.estado}</div>
              <div class="gc-timeline-date">${c.ultimaActualizacion || ''}</div>
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

// ── NOTAS (local) ───────────────────────────────

function cargarNotas(id) {
  const list = $('#gc-notas-list');
  if (!list) return;
  const notas = JSON.parse(localStorage.getItem('gc_notas_' + id) || '[]');
  if (notas.length === 0) {
    list.innerHTML = '<div class="gc-event-no-data">Sin notas aún</div>';
    return;
  }
  list.innerHTML = notas.reverse().map(n => `
    <div class="gc-nota-item">
      <div class="gc-nota-text">${n.texto}</div>
      <div class="gc-nota-time">${n.fecha}</div>
    </div>
  `).join('');
}

function agregarNota(id) {
  const input = $('#gc-nota-input');
  const texto = input.value.trim();
  if (!texto) return;
  const notas = JSON.parse(localStorage.getItem('gc_notas_' + id) || '[]');
  notas.push({ texto, fecha: new Date().toLocaleDateString('es-ES', { day:'numeric', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' }) });
  localStorage.setItem('gc_notas_' + id, JSON.stringify(notas));
  input.value = '';
  cargarNotas(id);
}

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

// ── MODALES ─────────────────────────────────────

function cerrarModal(id) {
  document.getElementById(id).style.display = 'none';
}

function abrirModalEditar(id) {
  fetch(`/gestion-candidatos/${id}`, { credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } })
    .then(r => r.json())
    .then(c => {
      document.getElementById('edit-id').value = id;
      document.getElementById('edit-nombre').value = c.nombre || '';
      document.getElementById('edit-apellido').value = c.apellido || '';
      document.getElementById('edit-email').value = c.email || '';
      document.getElementById('edit-telefono').value = c.telefono || '';
      document.getElementById('edit-cargo').value = c.cargo || '';
      document.getElementById('edit-ciudad').value = c.ciudad || '';
      document.getElementById('edit-experiencia').value = c.experiencia || 0;
      document.getElementById('edit-disponibilidad').value = c.disponibilidad || '';
      document.getElementById('edit-tecnologias').value = c.tecnologias || '';
      document.getElementById('edit-idiomas').value = c.idiomas || '';
      document.getElementById('edit-procesoActual').value = c.procesoActual || '';
      document.getElementById('modalEditarGc').style.display = 'flex';
    })
    .catch(err => alert('Error al cargar datos: ' + err.message));
}

function guardarEdicion() {
  const id = document.getElementById('edit-id').value;
  const data = {
    nombre: document.getElementById('edit-nombre').value.trim(),
    apellido: document.getElementById('edit-apellido').value.trim(),
    email: document.getElementById('edit-email').value.trim(),
    telefono: document.getElementById('edit-telefono').value.trim(),
    cargo: document.getElementById('edit-cargo').value.trim(),
    ciudad: document.getElementById('edit-ciudad').value.trim(),
    experiencia: document.getElementById('edit-experiencia').value,
    disponibilidad: document.getElementById('edit-disponibilidad').value,
    tecnologias: document.getElementById('edit-tecnologias').value.trim(),
    idiomas: document.getElementById('edit-idiomas').value.trim(),
    procesoActual: document.getElementById('edit-procesoActual').value
  };

  fetch(`/gestion-candidatos/${id}/editar`, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' },
    body: JSON.stringify(data)
  })
    .then(r => r.json())
    .then(res => {
      if (res.success) {
        cerrarModal('modalEditarGc');
        loadPage(currentPage);
        if (selectedCandidatoId == id) openDrawer(id);
        loadStats();
      } else {
        alert(res.error || 'Error al guardar');
      }
    })
    .catch(err => alert('Error al guardar: ' + err.message));
}

// ── MODAL ESTADO ────────────────────────────────

let estadoModalId = null;

function abrirModalEstado(id, nombre) {
  estadoModalId = id;
  document.getElementById('modalEstadoNombre').textContent = 'Candidato: ' + nombre;
  document.getElementById('modalEstadoSelect').value = '';
  document.getElementById('modalEstadoBtn').onclick = function() {
    const estado = document.getElementById('modalEstadoSelect').value;
    if (!estado) { alert('Selecciona un estado'); return; }
    cambiarEstado(estadoModalId, estado);
    cerrarModal('modalEstadoGc');
  };
  document.getElementById('modalEstadoGc').style.display = 'flex';
}

// ── MODAL COMPARTIR ─────────────────────────────

function abrirModalCompartir(id, email, nombre) {
  document.getElementById('compartirNombre').textContent = 'Compartir expediente de: ' + nombre;
  document.getElementById('compartirLink').value = window.location.origin + '/drive?folder=Candidatos/' + encodeURIComponent(email);
  document.getElementById('compartirEmail').value = '';
  document.getElementById('compartirEmail').dataset.candidatoId = id;
  document.getElementById('modalCompartirGc').style.display = 'flex';
}

function copiarEnlace(btn) {
  const input = document.getElementById('compartirLink');
  input.select();
  navigator.clipboard.writeText(input.value).then(() => {
    const orig = btn.innerHTML;
    btn.innerHTML = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>';
    btn.style.borderColor = '#16A34A';
    btn.style.color = '#16A34A';
    btn.style.background = '#F0FDF4';
    setTimeout(() => { btn.innerHTML = orig; btn.style.borderColor = ''; btn.style.color = ''; btn.style.background = ''; }, 2500);
  }).catch(() => {
    document.execCommand('copy');
  });
}

function enviarEnlace(btn) {
  const email = document.getElementById('compartirEmail').value.trim();
  if (!email || !email.includes('@')) {
    alert('Ingresa un correo válido');
    return;
  }
  const link = document.getElementById('compartirLink').value;
  const orig = btn.textContent;
  btn.textContent = '✓ Enviado';
  btn.style.background = '#16A34A';
  setTimeout(() => { btn.textContent = orig; btn.style.background = ''; }, 2500);
}

// ── ELIMINAR ────────────────────────────────────

function eliminarCandidato(id, nombre) {
  if (!confirm('¿Estás seguro de eliminar a ' + nombre + '?\nEsta acción no se puede deshacer.')) return;
  fetch(`/gestion-candidatos/${id}/eliminar`, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'X-Requested-With': 'XMLHttpRequest' }
  })
    .then(r => r.json())
    .then(res => {
      if (res.success) {
        closeDrawer();
        loadPage(currentPage);
        loadStats();
      } else {
        alert('Error al eliminar');
      }
    })
    .catch(err => alert('Error al eliminar: ' + err.message));
}

// ── MODAL OVERLAY CLOSE ─────────────────────────

document.addEventListener('click', function(e) {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.style.display = 'none';
  }
});

// ── NOTIFICACIONES ───────────────────────────────

function cargarNotificaciones() {
  const list = document.querySelector('#gc-notif-panel .gc-notif-list');
  const badge = document.getElementById('gc-notif-badge');
  if (!list) return;

  fetch('/notificaciones')
    .then(r => r.json())
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
    });
}

function marcarNotificacion(id) {
  fetch('/notificaciones/' + id + '/leer', { method: 'POST' })
    .then(r => r.json())
    .then(() => cargarNotificaciones());
}

function marcarTodasNotificaciones() {
  fetch('/notificaciones/leer-todas', { method: 'POST' })
    .then(r => r.json())
    .then(() => cargarNotificaciones());
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

// ── DOCUMENT CLOSE NOTIF PANEL ───────────────────

document.addEventListener('click', function(e) {
  const panel = document.getElementById('gc-notif-panel');
  const btn = document.getElementById('gc-notif-btn');
  if (panel && btn && !panel.contains(e.target) && !btn.contains(e.target)) {
    panel.classList.remove('gc-notif-open');
    btn.classList.remove('gc-notif-btn-active');
  }
});

// ── INIT ────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  loadStats();
  loadPage(0);
  cargarNotificaciones();
  setInterval(cargarNotificaciones, 30000);
});
