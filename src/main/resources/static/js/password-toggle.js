/**
 * FlowMatic - Utilidad de Visibilidad y Confirmación de Contraseñas
 */

function togglePasswordVisibility(inputId, btn) {
    var input = document.getElementById(inputId);
    if (!input) return;
    
    var isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    
    var eyeOpenSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>';
    var eyeOffSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>';
    
    btn.innerHTML = isPassword ? eyeOffSvg : eyeOpenSvg;
    btn.setAttribute('aria-label', isPassword ? 'Ocultar contraseña' : 'Mostrar contraseña');
    btn.setAttribute('title', isPassword ? 'Ocultar contraseña' : 'Mostrar contraseña');
}

function initPasswordMatch(pwdId, confirmPwdId, feedbackId) {
    var pwdInput = document.getElementById(pwdId);
    var confirmInput = document.getElementById(confirmPwdId);
    var feedback = document.getElementById(feedbackId);
    
    if (!pwdInput || !confirmInput) return;
    
    function checkMatch() {
        var p1 = pwdInput.value;
        var p2 = confirmInput.value;
        
        if (!feedback) return true;

        if (!p2) {
            feedback.textContent = '';
            feedback.className = 'password-match-feedback';
            return true;
        }
        
        if (p1 === p2) {
            feedback.textContent = '✓ Las contraseñas coinciden';
            feedback.className = 'password-match-feedback password-match-ok';
            return true;
        } else {
            feedback.textContent = '✗ Las contraseñas no coinciden';
            feedback.className = 'password-match-feedback password-match-error';
            return false;
        }
    }
    
    pwdInput.addEventListener('input', checkMatch);
    confirmInput.addEventListener('input', checkMatch);
    
    var form = confirmInput.closest('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            if (pwdInput.value && confirmInput.value && pwdInput.value !== confirmInput.value) {
                e.preventDefault();
                checkMatch();
                confirmInput.focus();
            }
        });
    }
}
