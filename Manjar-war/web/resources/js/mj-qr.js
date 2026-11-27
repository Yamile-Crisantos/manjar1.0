/*
 * Página del código QR (qr.xhtml). Dibuja el QR en el navegador con qrcode.js
 * (qrcode-generator, licencia MIT) a partir de:
 *   - la dirección del servidor (por defecto, la de esta misma página), y
 *   - la ruta del menú del negocio, que entrega el servidor en data-ruta.
 * Permite descargar la imagen en PNG, imprimir la hoja y copiar el enlace (mj-copiar.js).
 * Si la dirección es la de la propia computadora (localhost), avisa que un celular no podrá abrirla y ofrece
 * las direcciones de la computadora en la red local.
 */
(function () {
    var COLOR_QR = '#2e2521';

    function el(id) {
        return document.getElementById(id);
    }

    function servidor() {
        var campo = el('mj-servidor');
        return ((campo && campo.value) || '').trim().replace(/\/+$/, '');
    }

    function enlaceCompleto() {
        return servidor() + el('mj-qr-datos').getAttribute('data-ruta');
    }

    function mostrarAviso(texto) {
        var aviso = el('mj-qr-aviso');
        aviso.textContent = texto || '';
        aviso.style.display = texto ? '' : 'none';
    }

    function dibujar() {
        var canvas = el('mj-qr');
        var texto = el('mj-qr-enlace');
        var base = servidor();
        var ctx = canvas.getContext('2d');

        // Solo direcciones http(s) con caracteres simples: así el QR siempre se lee bien
        if (!/^https?:\/\/[\x21-\x7e]+$/.test(base)) {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            canvas.setAttribute('data-listo', 'no');
            texto.textContent = '';
            mostrarAviso('Escribe la dirección del servidor, por ejemplo http://192.168.1.20:8080');
            return;
        }
        mostrarAviso('');

        var url = enlaceCompleto();
        var qr = qrcode(0, 'M');
        qr.addData(url);
        qr.make();

        var modulos = qr.getModuleCount();
        var margen = 4;
        var celda = Math.max(3, Math.floor(400 / (modulos + 2 * margen)));
        var lado = (modulos + 2 * margen) * celda;
        canvas.width = lado;
        canvas.height = lado;
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, lado, lado);
        ctx.fillStyle = COLOR_QR;
        for (var fila = 0; fila < modulos; fila++) {
            for (var col = 0; col < modulos; col++) {
                if (qr.isDark(fila, col)) {
                    ctx.fillRect((col + margen) * celda, (fila + margen) * celda, celda, celda);
                }
            }
        }
        canvas.setAttribute('data-listo', 'si');
        canvas.setAttribute('data-url', url);
        texto.textContent = url;
        el('mj-qr-abrir').setAttribute('href', url);

        var local = /^https?:\/\/(localhost|127\.0\.0\.1|\[::1\])(:|\/|$)/i.test(url);
        el('mj-qr-local').style.display = local ? '' : 'none';
    }

    window.mjDibujarQr = dibujar;

    window.mjDescargarQr = function () {
        var canvas = el('mj-qr');
        if (canvas.getAttribute('data-listo') !== 'si') {
            return;
        }
        var enlace = document.createElement('a');
        enlace.href = canvas.toDataURL('image/png');
        enlace.download = 'qr-' + el('mj-qr-datos').getAttribute('data-codigo') + '.png';
        document.body.appendChild(enlace);
        enlace.click();
        document.body.removeChild(enlace);
    };

    window.mjImprimirQr = function () {
        window.print();
    };

    document.addEventListener('DOMContentLoaded', function () {
        var campo = el('mj-servidor');
        if (!campo) {
            return;
        }
        // Por omisión, la dirección pública configurada en el servidor (-Dmanjar.url.publica) o, si no hay, la de esta página
        var base = el('mj-qr-datos').getAttribute('data-base');
        campo.value = base || window.location.origin;
        campo.addEventListener('input', dibujar);
        // Direcciones de la computadora en la red: un toque las pone en el campo
        document.addEventListener('click', function (e) {
            var b = e.target && e.target.closest ? e.target.closest('.mj-qr-red') : null;
            if (b) {
                campo.value = b.getAttribute('data-url');
                dibujar();
            }
        });
        dibujar();
    });
})();
