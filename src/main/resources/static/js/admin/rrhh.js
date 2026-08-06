function abrirDrawerRRHH(id) {
  const drawer = document.getElementById('drawerRRHH');
  const grid = document.querySelector('.gc-main-grid');
  const content = document.getElementById('drawerRRHHContent');

  grid.classList.add('drawer-open');
  content.innerHTML = '<div class="gc-drawer-empty">Cargando...</div>';

  fetch(`/admin/api/rrhh/${id}`)
    .then(r => r.json())
    .then(data => {
      const ini = data.nombre ? data.nombre.substring(0, 1).toUpperCase() : 'U';
      const badgeClass = data.activo ? 'gc-badge-success' : 'gc-badge-danger';
      const status = data.activo ? 'Activo' : 'Bloqueado';

      content.innerHTML = `
        <div style="display:flex; flex-direction:column; align-items:center; gap:16px; margin-bottom:32px;">
            <div class="gc-avatar" style="width: 80px; height: 80px; font-size: 28px;">${ini}</div>
            <div style="text-align:center;">
                <h4 style="margin:0; font-weight:700; color:#0F172A;">${data.nombre || ''} ${data.apellido || ''}</h4>
                <p style="margin:4px 0 0; color:#64748B; font-size:14px;">${data.email}</p>
            </div>
            <span class="gc-badge ${badgeClass}">${status}</span>
        </div>
        
        <h5 style="font-size:12px; text-transform:uppercase; color:#94A3B8; font-weight:700; margin-bottom:16px; letter-spacing:0.05em;">Información Profesional</h5>
        <div style="background:#F8FAFC; border-radius:12px; padding:16px; border:1px solid #F1F5F9; display:flex; flex-direction:column; gap:16px;">
            <div>
                <p style="font-size:12px; color:#64748B; margin:0 0 4px;">Cargo</p>
                <p style="font-size:14px; color:#0F172A; font-weight:500; margin:0;">${data.cargo || '-'}</p>
            </div>
            <div>
                <p style="font-size:12px; color:#64748B; margin:0 0 4px;">Documento</p>
                <p style="font-size:14px; color:#0F172A; font-weight:500; margin:0;">${data.documento || '-'}</p>
            </div>
            <div>
                <p style="font-size:12px; color:#64748B; margin:0 0 4px;">Último Acceso</p>
                <p style="font-size:14px; color:#0F172A; font-weight:500; margin:0;">${data.ultimoAcceso ? new Date(data.ultimoAcceso).toLocaleString() : '-'}</p>
            </div>
        </div>
      `;
    })
    .catch(e => {
        content.innerHTML = '<div class="gc-drawer-empty" style="color:red;">Error al cargar datos</div>';
    });
}

function cerrarDrawerRRHH() {
  document.querySelector('.gc-main-grid').classList.remove('drawer-open');
}

function editarRRHH(id) {
  fetch(`/admin/api/rrhh/${id}`)
    .then(r => r.json())
    .then(data => {
        document.getElementById('rrhhId').value = data.id;
        document.getElementById('rrhhNombre').value = data.nombre || '';
        document.getElementById('rrhhApellido').value = data.apellido || '';
        document.getElementById('rrhhEmail').value = data.email || '';
        document.getElementById('rrhhClave').value = ''; 
        document.getElementById('rrhhClave').removeAttribute('required');
        document.getElementById('pwdHint').style.display = 'inline';
        document.getElementById('rrhhTelefono').value = data.telefono || '';
        document.getElementById('rrhhDocumento').value = data.documento || '';
        document.getElementById('rrhhCargo').value = data.cargo || '';
        
        document.getElementById('modalRRHHTitle').textContent = 'Editar RRHH';
        var myModal = new bootstrap.Modal(document.getElementById('modalEditarRRHH'));
        myModal.show();
    });
}

function toggleOptions(btn) {
    const menus = document.querySelectorAll('.gc-dropdown-menu');
    menus.forEach(m => {
        if (m !== btn.nextElementSibling) m.classList.remove('show');
    });
    btn.nextElementSibling.classList.toggle('show');
}

document.addEventListener('click', function(e) {
    if(!e.target.closest('.gc-action-more')) {
        document.querySelectorAll('.gc-dropdown-menu').forEach(m => m.classList.remove('show'));
    }
});

function guardarRRHH() {
    const id = document.getElementById('rrhhId').value;
    const data = {
        nombre: document.getElementById('rrhhNombre').value,
        apellido: document.getElementById('rrhhApellido').value,
        email: document.getElementById('rrhhEmail').value,
        clave: document.getElementById('rrhhClave').value,
        telefono: document.getElementById('rrhhTelefono').value,
        documento: document.getElementById('rrhhDocumento').value,
        cargo: document.getElementById('rrhhCargo').value
    };

    const method = 'PUT';
    const url = `/admin/api/rrhh/${id}`;

    // Asumimos que el token CSRF está en el meta si lo necesitamos, o lo omitimos para dev si no aplica a API.
    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(async res => {
        if(res.ok) {
            window.location.reload();
        } else {
            const err = await res.text();
            alert('Error al guardar el usuario: ' + err);
        }
    })
    .catch(e => {
        alert('Error de red: ' + e);
    });
}

function cambiarEstadoRRHH(id) {
    if(confirm('¿Seguro que deseas cambiar el estado de acceso de este usuario?')) {
        fetch(`/admin/api/rrhh/${id}/estado`, { method: 'PUT' })
        .then(r => { if(r.ok) window.location.reload(); });
    }
}

function toggleBloqueoRRHH(id) {
    if(confirm('¿Estás seguro de que deseas modificar el estado de bloqueo de este usuario?')) {
        fetch(`/admin/api/rrhh/${id}/toggle-bloqueo`, { method: 'PUT' })
        .then(r => { if(r.ok) window.location.reload(); });
    }
}

function eliminarRRHH(id) {
    if(confirm('¿Seguro que deseas ELIMINAR permanentemente a este usuario?')) {
        fetch(`/admin/api/rrhh/${id}`, { method: 'DELETE' })
        .then(r => { if(r.ok) window.location.reload(); });
    }
}
