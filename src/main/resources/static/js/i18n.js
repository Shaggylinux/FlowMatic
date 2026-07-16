(function () {
  'use strict';

  var STORAGE_KEY = 'flowmatic_lang';
  var currentLang = localStorage.getItem(STORAGE_KEY) || 'es';

  var translations = {
    /* ─── HOME PAGE ─────────────────────────────── */
    'Caracter\u00edsticas': 'Features',
    'Pipeline': 'Pipeline',
    'Contacto': 'Contact',
    'Iniciar Sesi\u00f3n': 'Log In',
    '\u2726 HR Tech Empresarial': '\u2726 Enterprise HR Tech',
    'Gestiona el Talento de tu Empresa con': 'Manage Your Company\u2019s Talent with',
    'Precisi\u00f3n y Seguridad': 'Precision & Security',
    'FLOWMATIC centraliza la contrataci\u00f3n, documentaci\u00f3n y seguimiento de candidatos en una sola plataforma segura. Dise\u00f1ado para equipos de RRHH que necesitan control total.': 'FLOWMATIC centralizes hiring, documentation, and candidate tracking in a single secure platform. Designed for HR teams that need full control.',
    'Ver caracter\u00edsticas': 'View Features',
    'Candidatos gestionados': 'Candidates managed',
    'Satisfacci\u00f3n cliente': 'Client satisfaction',
    'Empresas conf\u00edan': 'Companies trust us',
    '\u00bfPor qu\u00e9 las empresas eligen': 'Why companies choose',
    '\u00bfPor qu\u00e9 las empresas eligen FLOWMATIC?': 'Why do companies choose FLOWMATIC?',
    'Seguridad de Datos': 'Data Security',
    'Cifrado de extremo a extremo, control de acceso por roles y auditor\u00eda completa de cada acci\u00f3n en la plataforma.': 'End-to-end encryption, role-based access, and complete audit of every action.',
    'Pipeline Visual': 'Visual Pipeline',
    'Visualiza el estado de cada candidato en tiempo real con nuestro pipeline de etapas personalizable.': 'View each candidate\u2019s status in real-time with customizable pipeline stages.',
    'Gesti\u00f3n Documental': 'Document Management',
    'Centraliza hojas de vida, contratos y certificaciones. Cada documento organizado y accesible al instante.': 'Centralize resumes, contracts, and certifications. Every document organized and instantly accessible.',
    'Reportes en Excel': 'Excel Reports',
    'Exporta datos de candidatos, m\u00e9tricas de contrataci\u00f3n y estados en formatos listos para presentar.': 'Export candidate data, hiring metrics, and statuses in presentation-ready formats.',
    'El journey del candidato, bajo control': 'The candidate journey, under control',
    'Registrado': 'Registered',
    'En Revisi\u00f3n': 'In Review',
    'Entrevista': 'Interview',
    'Seleccionado': 'Selected',
    'Incorporado': 'Hired',
    'Cont\u00e1ctanos': 'Contact Us',
    'Estamos para ayudarte en todo momento.': 'We\u2019re here to help you anytime.',
    'Nombre': 'Name',
    'Correo': 'Email',
    'Mensaje': 'Message',
    'Enviar mensaje': 'Send Message',
    'FLOWMATIC': 'FLOWMATIC',
    'Plataforma empresarial de gesti\u00f3n de talento, documentos y procesos de selecci\u00f3n.': 'Enterprise platform for talent management, documents, and recruitment processes.',
    'Producto': 'Product',
    'Acceso': 'Access',
    '\u00a9 2026 FLOWMATIC. Todos los derechos reservados.': '\u00a9 2026 FLOWMATIC. All rights reserved.',
    'Ir al inicio': 'Go to home',
    'Ir al Drive': 'Go to Drive',
    '\u00bfEn qu\u00e9 podemos ayudarte?': 'How can we help you?',

    /* ─── LOGIN PAGE ────────────────────────────── */
    'Bienvenido de vuelta': 'Welcome back',
    'Iniciar Sesi\u00f3n': 'Log In',
    'Correo o contrase\u00f1a incorrectos': 'Incorrect email or password',
    'Sesi\u00f3n cerrada correctamente': 'Logged out successfully',
    'Correo electr\u00f3nico': 'Email',
    'Contrase\u00f1a': 'Password',
    '\u00bfOlvidaste tu contrase\u00f1a?': 'Forgot your password?',
    'Ingresar': 'Sign In',
    'Gesti\u00f3n de Talento Inteligente': 'Smart Talent Management',
    'Acceso seguro con roles': 'Secure role-based access',
    'Documentos centralizados': 'Centralized documents',
    'Pipeline de candidatos en tiempo real': 'Real-time candidate pipeline',

    /* ─── REGISTRO CANDIDATO ────────────────────── */
    'Crear cuenta': 'Create Account',
    'Cuenta creada. Revisa tu correo para activarla.': 'Account created. Check your email to activate it.',
    'Este correo ya est\u00e1 registrado.': 'This email is already registered.',
    'Apellido': 'Last Name',
    'Tel\u00e9fono': 'Phone',
    'Registrarme': 'Register',
    '\u00bfYa tienes cuenta?': 'Already have an account?',
    'Inicia sesi\u00f3n': 'Log in',
    'Tu proceso de selecci\u00f3n, digitalizado': 'Your recruitment process, digitized',
    'Sube tus documentos f\u00e1cilmente': 'Upload your documents easily',
    'Sigue el estado de tu candidatura': 'Track your application status',
    'Comunicaci\u00f3n directa con RRHH': 'Direct communication with HR',

    /* ─── ADMIN PAGE ────────────────────────────── */
    'Dashboard': 'Dashboard',
    'RRHH': 'HR',
    'Cerrar Sesi\u00f3n': 'Log Out',
    'Panel de Administraci\u00f3n': 'Admin Panel',
    'Gesti\u00f3n de usuarios y personal': 'User and staff management',
    'Exportar Excel': 'Export to Excel',
    'Registrar Personal de RRHH': 'Register HR Staff',
    'Email': 'Email',
    'Crear RRHH': 'Create HR',
    'ID': 'ID',
    'Rol': 'Role',
    'Estado': 'Status',
    'Acciones': 'Actions',
    'Activo': 'Active',
    'Pendiente': 'Pending',
    'Eliminar': 'Delete',
    'Editar': 'Edit',
    'Editar Usuario': 'Edit User',
    'Nueva Contrase\u00f1a (opcional)': 'New Password (optional)',
    'Cancelar': 'Cancel',
    'Guardar Cambios': 'Save Changes',
    'Cerrar': 'Close',
    'Solo letras y espacios': 'Letters and spaces only',
    'Solo n\u00fameros, entre 7 y 15 d\u00edgitos': 'Numbers only, 7-15 digits',
    'M\u00ednimo 8 caracteres': 'Minimum 8 characters',

    /* ─── DRIVE (RRHH / CANDIDATO) ──────────────── */
    'Principal': 'Home',
    'Acciones': 'Actions',
    'Nueva Carpeta': 'New Folder',
    'Registrar Candidato': 'Register Candidate',
    'Mis Carpetas': 'My Folders',
    'Sin carpetas a\u00fan.': 'No folders yet.',
    'Archivos en:': 'Files in:',
    'Ra\u00edz': 'Root',
    'Mis Documentos': 'My Documents',
    'Mi Estado Actual': 'My Current Status',
    'Tu proceso est\u00e1 siendo gestionado por el equipo de Selecci\u00f3n.': 'Your process is being managed by the Selection team.',
    'Candidatos activos': 'Active Candidates',
    'Documentos subidos': 'Documents uploaded',
    'Carpetas creadas': 'Folders created',
    'Archivos': 'Files',
    'Elegir archivo': 'Choose file',
    'Subir': 'Upload',
    'Descargar': 'Download',
    'Compartir': 'Share',
    'Esta carpeta est\u00e1 vac\u00eda.': 'This folder is empty.',
    'A\u00fan no has recibido archivos compartidos.': 'You haven\u2019t received shared files yet.',
    'Gesti\u00f3n de Candidatos': 'Candidate Management',
    'Nombre Completo': 'Full Name',
    'Acci\u00f3n': 'Action',
    'Actualizar': 'Update',
    'No hay candidatos registrados todav\u00eda.': 'No candidates registered yet.',
    'Registrar Nuevo Candidato': 'Register New Candidate',
    'Registrar': 'Register',
    'En pruebas': 'In Testing',
    'Contratado': 'Hired',
    'No aceptado': 'Not Accepted',
    'Ingresa el nombre de la nueva carpeta:': 'Enter the new folder name:',
    'Correo del destinatario:': 'Recipient\u2019s email:',
    '\u00bfEliminar este usuario?': 'Delete this user?',
    '\u00bfEliminar archivo?': 'Delete file?',
    'Error al registrar': 'Registration error',

    /* ─── CANDIDATO PAGE ────────────────────────── */
    'Panel de Candidato': 'Candidate Panel',
    'Bienvenido a Flowmatic': 'Welcome to Flowmatic',
    'Vista en construcci\u00f3n': 'View under construction',
    'Accede a tus documentos desde el Drive.': 'Access your documents from the Drive.',

    /* ─── FORGOT PASSWORD ───────────────────────── */
    'Recuperar contrase\u00f1a': 'Recover Password',
    'Ingresa tu correo y te enviaremos un enlace para restablecer tu contrase\u00f1a.': 'Enter your email and we\u2019ll send you a link to reset your password.',
    'Revisa tu correo para continuar': 'Check your email to continue',
    'Enviar enlace': 'Send Link',
    '\u2190 Volver al inicio de sesi\u00f3n': '\u2190 Back to log in',
    'Flowmatic \u2014 Gesti\u00f3n de Talento': 'Flowmatic \u2014 Talent Management',

    /* ─── RESET PASSWORD ────────────────────────── */
    'Nueva contrase\u00f1a': 'New Password',
    'Ingresa tu nueva contrase\u00f1a para continuar.': 'Enter your new password to continue.',
    'Contrase\u00f1a actualizada correctamente': 'Password updated successfully',
    'La contrase\u00f1a debe tener m\u00ednimo 8 caracteres': 'Password must be at least 8 characters',
    'Cambiar contrase\u00f1a': 'Change Password',
    '\u2190 Ir al inicio de sesi\u00f3n': '\u2190 Go to log in',

    /* ─── ACTIVACION ────────────────────────────── */
    '\u00a1Cuenta Activada!': 'Account Activated!',
    'Tu cuenta ha sido activada exitosamente. Ya puedes iniciar sesi\u00f3n con tus credenciales.': 'Your account has been successfully activated. You can now log in with your credentials.',
    'Volver al Inicio': 'Back to Home',
    'Enlace Inv\u00e1lido': 'Invalid Link',
    'El enlace de activaci\u00f3n es inv\u00e1lido o ya fue utilizado. Por favor, intenta registrarte nuevamente.': 'The activation link is invalid or has already been used. Please try registering again.',
    'Registrarse': 'Register',
    'Ir al Inicio': 'Go to Home',

    /* ─── FLOWMATIC BRAND ───────────────────────── */
    'Flowmatic': 'Flowmatic'
  };

  var originalTexts = new WeakMap();

  function normalize(text) {
    return text.replace(/\s+/g, ' ').trim();
  }

  function isInViewport(el) {
    var rect = el.getBoundingClientRect();
    return (
      rect.top < (window.innerHeight || document.documentElement.clientHeight) &&
      rect.bottom > 0
    );
  }

  function translateToEnglish(root) {
    var walker = document.createTreeWalker(
      root,
      NodeFilter.SHOW_TEXT,
      {
        acceptNode: function (node) {
          if (!node.textContent.trim()) return NodeFilter.FILTER_REJECT;
          var parent = node.parentElement;
          if (!parent || parent.closest('script, style, noscript, svg, .loader-overlay, #lang-toggle-btn')) return NodeFilter.FILTER_REJECT;
          if (parent.tagName === 'OPTION' || parent.tagName === 'SELECT') return NodeFilter.FILTER_ACCEPT;
          if (getComputedStyle(parent).display === 'none') return NodeFilter.FILTER_REJECT;
          if (parent.isContentEditable) return NodeFilter.FILTER_REJECT;
          return NodeFilter.FILTER_ACCEPT;
        }
      },
      false
    );

    var nodesToTranslate = [];
    while (walker.nextNode()) {
      var node = walker.currentNode;
      var key = normalize(node.textContent);
      if (translations[key]) {
        nodesToTranslate.push({ node: node, key: key });
      }
    }

    for (var i = 0; i < nodesToTranslate.length; i++) {
      var item = nodesToTranslate[i];
      if (!originalTexts.has(item.node)) {
        originalTexts.set(item.node, item.node.textContent);
      }
      item.node.textContent = translations[item.key];
    }

    localStorage.setItem(STORAGE_KEY, 'en');
    currentLang = 'en';
  }

  function translateToSpanish(root) {
    var walker = document.createTreeWalker(
      root,
      NodeFilter.SHOW_TEXT,
      {
        acceptNode: function (node) {
          if (!node.textContent.trim()) return NodeFilter.FILTER_REJECT;
          var parent = node.parentElement;
          if (!parent || parent.closest('script, style, noscript, svg, .loader-overlay, #lang-toggle-btn')) return NodeFilter.FILTER_REJECT;
          if (getComputedStyle(parent).display === 'none') return NodeFilter.FILTER_REJECT;
          return originalTexts.has(node) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT;
        }
      },
      false
    );

    var nodesToRestore = [];
    while (walker.nextNode()) {
      nodesToRestore.push(walker.currentNode);
    }

    for (var i = 0; i < nodesToRestore.length; i++) {
      nodesToRestore[i].textContent = originalTexts.get(nodesToRestore[i]);
    }

    localStorage.setItem(STORAGE_KEY, 'es');
    currentLang = 'es';
  }

  function toggle() {
    if (currentLang === 'es') {
      translateToEnglish(document.body);
    } else {
      translateToSpanish(document.body);
    }
    updateButton();
  }

  function updateButton() {
    var btn = document.getElementById('lang-toggle-btn');
    if (!btn) return;
    if (currentLang === 'es') {
      btn.innerHTML = '<span style="margin-right:4px;">\ud83c\uddfa\ud83c\uddf8</span> EN';
      btn.title = 'Switch to English';
    } else {
      btn.innerHTML = '<span style="margin-right:4px;">\ud83c\uddea\ud83c\uddf8</span> ES';
      btn.title = 'Cambiar a Espa\u00f1ol';
    }
  }

  function addToggleButton() {
    if (document.getElementById('lang-toggle-btn')) return;

    var btn = document.createElement('button');
    btn.id = 'lang-toggle-btn';
    btn.onclick = toggle;
    btn.setAttribute('aria-label', 'Toggle language');

    var style = btn.style;
    style.position = 'fixed';
    style.bottom = '20px';
    style.right = '20px';
    style.zIndex = '99999';
    style.padding = '10px 16px';
    style.borderRadius = '10px';
    style.border = '1px solid rgba(13, 148, 136, 0.25)';
    style.background = 'rgba(255,255,255,0.95)';
    style.color = '#0D9488';
    style.cursor = 'pointer';
    style.fontSize = '13px';
    style.fontWeight = '600';
    style.fontFamily = "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif";
    style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
    style.backdropFilter = 'blur(8px)';
    style.webkitBackdropFilter = 'blur(8px)';
    style.transition = 'all 0.2s ease';
    style.lineHeight = '1';
    style.display = 'flex';
    style.alignItems = 'center';
    style.gap = '4px';
    style.letterSpacing = '0.01em';

    btn.onmouseenter = function () {
      btn.style.background = 'rgba(240, 253, 250, 0.98)';
      btn.style.borderColor = 'rgba(13, 148, 136, 0.5)';
      btn.style.transform = 'translateY(-1px)';
      btn.style.boxShadow = '0 6px 16px rgba(0,0,0,0.15)';
    };
    btn.onmouseleave = function () {
      btn.style.background = 'rgba(255,255,255,0.95)';
      btn.style.borderColor = 'rgba(13, 148, 136, 0.25)';
      btn.style.transform = 'none';
      btn.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
    };

    document.body.appendChild(btn);
    updateButton();
  }

  function init() {
    addToggleButton();
    if (currentLang === 'en') {
      if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () {
          translateToEnglish(document.body);
        });
      } else {
        translateToEnglish(document.body);
      }
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();

// ── REGISTRAR CANDIDATO DESDE MODAL ──────────────

function enviarRegistro(modalId, formId) {
  var modal = document.getElementById(modalId);
  var form = document.getElementById(formId);
  if (!modal || !form) return;
  var datos = Object.fromEntries(new FormData(form).entries());
  fetch('/registro/candidato/api', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datos)
  }).then(function(res) {
    if (res.ok) {
      modal.style.display = 'none';
      form.reset();
      location.reload();
    } else {
      alert('Error al registrar candidato');
    }
  }).catch(function() {
    alert('Error de conexión al registrar candidato');
  });
}
