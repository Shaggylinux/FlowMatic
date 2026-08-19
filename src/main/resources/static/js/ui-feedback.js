/**
 * FlowMatic - UI Feedback & Interaction Utilities
 * 1. Button Loading State (Prevents double submit)
 * 2. Floating Toasts with smooth auto-dismiss
 */

(function () {
  'use strict';

  // ── 1. PREVENCIÓN DE DOBLE ENVÍO (BUTTON LOADING STATE) ──────────────
  document.addEventListener('DOMContentLoaded', function () {
    // Interceptar todos los formularios estándar
    const forms = document.querySelectorAll('form:not([data-no-loading])');

    forms.forEach(function (form) {
      form.addEventListener('submit', function (e) {
        // Si el formulario ya está siendo enviado o es inválido en HTML5, no hacer nada
        if (form.dataset.submitting === 'true') {
          e.preventDefault();
          return false;
        }

        if (!form.checkValidity()) {
          return;
        }

        const submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');
        if (submitBtn) {
          form.dataset.submitting = 'true';
          submitBtn.classList.add('is-loading');
          submitBtn.setAttribute('disabled', 'disabled');

          // Si es un botón, guardar contenido previo e inyectar spinner
          if (submitBtn.tagName.toLowerCase() === 'button') {
            submitBtn.dataset.originalContent = submitBtn.innerHTML;
            submitBtn.innerHTML = '<span class="btn-spinner"></span> <span>Procesando...</span>';
          }
        }
      });
    });

    // ── 2. AUTO-DISMISS DE ALERTAS Y TOASTS EXISTENTES ─────────────────
    autoDismissServerToasts();
  });

  // ── 3. SISTEMA DE TOASTS FLOTANTES UNIFICADO ────────────────────────
  function createToastContainer() {
    let container = document.getElementById('fm-toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'fm-toast-container';
      container.className = 'fm-toast-container';
      document.body.appendChild(container);
    }
    return container;
  }

  window.showToast = function (tipo, mensaje, duracionMs) {
    const container = createToastContainer();
    const duration = duracionMs || 4500;

    const toast = document.createElement('div');
    toast.className = 'fm-toast fm-toast-' + (tipo || 'info');

    let iconSvg = '';
    if (tipo === 'success') {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>';
    } else if (tipo === 'error') {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>';
    } else if (tipo === 'warning') {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>';
    } else {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>';
    }

    toast.innerHTML = `
      <div class="fm-toast-icon">${iconSvg}</div>
      <div class="fm-toast-message">${mensaje}</div>
      <button type="button" class="fm-toast-close" aria-label="Cerrar">&times;</button>
    `;

    container.appendChild(toast);

    // Animación de entrada
    requestAnimationFrame(function () {
      toast.classList.add('fm-toast-visible');
    });

    // Botón de cerrar
    const closeBtn = toast.querySelector('.fm-toast-close');
    function dismiss() {
      toast.classList.remove('fm-toast-visible');
      toast.classList.add('fm-toast-hiding');
      setTimeout(function () {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast);
        }
      }, 300);
    }

    if (closeBtn) {
      closeBtn.addEventListener('click', dismiss);
    }

    // Auto-cierre
    setTimeout(dismiss, duration);
  };

  function autoDismissServerToasts() {
    const serverToasts = document.querySelectorAll('.cfg-toast');
    serverToasts.forEach(function (toast) {
      // Auto-ocultar después de 4.5 segundos
      setTimeout(function () {
        toast.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-10px)';
        setTimeout(function () {
          if (toast.parentNode) {
            toast.style.display = 'none';
          }
        }, 400);
      }, 4500);
    });
  }
})();
