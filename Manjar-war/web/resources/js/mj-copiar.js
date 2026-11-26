/*
 * Botón "Copiar enlace": cualquier botón con  data-copiar-de="idDelElemento"  copia el texto de ese elemento
 * (o su valor, si es un campo) al portapapeles. Si el navegador no deja usar el portapapeles (por ejemplo,
 * en una dirección http sin certificado) se selecciona el texto para que se copie con "Copiar" del sistema.
 */
(function () {
    function texto(el) {
        return ((el.value !== undefined && el.tagName !== 'CODE') ? el.value : el.textContent || '').trim();
    }

    function avisar(boton, mensaje) {
        var original = boton.getAttribute('data-original') || boton.textContent;
        boton.setAttribute('data-original', original);
        boton.textContent = mensaje;
        window.setTimeout(function () {
            boton.textContent = original;
        }, 2200);
    }

    function seleccionar(el) {
        try {
            var r = document.createRange();
            r.selectNodeContents(el);
            var s = window.getSelection();
            s.removeAllRanges();
            s.addRange(r);
        } catch (e) {
            // sin selección: no pasa nada
        }
    }

    function copiarConTextarea(t) {
        var ta = document.createElement('textarea');
        ta.value = t;
        ta.setAttribute('readonly', '');
        ta.style.position = 'fixed';
        ta.style.opacity = '0';
        document.body.appendChild(ta);
        ta.select();
        var ok = false;
        try {
            ok = document.execCommand('copy');
        } catch (e) {
            ok = false;
        }
        document.body.removeChild(ta);
        return ok;
    }

    document.addEventListener('click', function (e) {
        var b = e.target && e.target.closest ? e.target.closest('[data-copiar-de]') : null;
        if (!b) {
            return;
        }
        var el = document.getElementById(b.getAttribute('data-copiar-de'));
        var t = el ? texto(el) : '';
        if (!t) {
            avisar(b, 'Nada que copiar');
            return;
        }
        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(t).then(function () {
                avisar(b, 'Enlace copiado');
            }, function () {
                avisar(b, copiarConTextarea(t) ? 'Enlace copiado' : 'Cópialo a mano');
                if (el) {
                    seleccionar(el);
                }
            });
        } else if (copiarConTextarea(t)) {
            avisar(b, 'Enlace copiado');
        } else {
            avisar(b, 'Cópialo a mano');
            if (el) {
                seleccionar(el);
            }
        }
    });
})();
