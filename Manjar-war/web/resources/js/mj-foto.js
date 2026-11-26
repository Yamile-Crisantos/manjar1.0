/*
 * Foto de un platillo (platillos.xhtml): el dueño la elige, la arrastra, la pega o escribe el enlace de una foto.
 *
 * Una foto elegida se reduce aquí mismo, en el navegador (lado de 900 px y JPEG), antes de enviarla. Así una foto de
 * 6 MB del celular viaja como una de ~100 KB, y el servidor la vuelve a revisar y a escribir como JPEG propio
 * (ver ImagenUtil). Los datos se dejan en dos campos ocultos del formulario:
 *   fotoArchivo : la foto reducida como "data:image/jpeg;base64,..."
 *   quitarFoto  : "true" si el dueño pidió quitar la foto
 * El campo con data-mj-foto agrupa todo; los eventos se escuchan en el documento porque el diálogo se vuelve a
 * dibujar cada vez que se abre.
 */
(function () {
    var LADO = 900;
    var CALIDAD = 0.82;
    var MAX_ORIGINAL = 25 * 1024 * 1024;
    var TIPOS = /^image\/(jpeg|png|webp|gif)$/;

    function campoDe(nodo) {
        return nodo && nodo.closest ? nodo.closest('[data-mj-foto]') : null;
    }

    function oculto(campo, sufijo) {
        var form = campo.closest('form');
        return form ? form.querySelector('input[id$=":' + sufijo + '"]') : null;
    }

    function enlaceInput(campo) {
        return campo.querySelector('.mj-foto-enlace input, input.mj-foto-enlace');
    }

    function aviso(campo, texto) {
        var a = campo.querySelector('.mj-foto-aviso');
        if (a) {
            a.textContent = texto || '';
            a.classList.toggle('oculto', !texto);
        }
    }

    function mostrar(campo, src) {
        var img = campo.querySelector('.mj-foto-vista');
        var vacio = campo.querySelector('.mj-foto-vacio');
        var quitar = campo.querySelector('.mj-foto-quitar');
        if (src) {
            img.src = src;
        } else {
            img.removeAttribute('src');
        }
        img.classList.toggle('oculto', !src);
        vacio.classList.toggle('oculto', !!src);
        campo.classList.toggle('con-foto', !!src);
        if (quitar) {
            quitar.classList.toggle('oculto', !src);
        }
    }

    function procesar(campo, archivo) {
        if (!archivo) {
            return;
        }
        if (!TIPOS.test(archivo.type)) {
            aviso(campo, 'Elige una imagen JPG, PNG, WEBP o GIF.');
            return;
        }
        if (archivo.size > MAX_ORIGINAL) {
            aviso(campo, 'Esa foto pesa demasiado. Elige una de menos de 25 MB.');
            return;
        }
        var direccion = URL.createObjectURL(archivo);
        var img = new Image();
        img.onload = function () {
            var escala = Math.min(1, LADO / Math.max(img.naturalWidth, img.naturalHeight));
            var w = Math.max(1, Math.round(img.naturalWidth * escala));
            var h = Math.max(1, Math.round(img.naturalHeight * escala));
            var lienzo = document.createElement('canvas');
            lienzo.width = w;
            lienzo.height = h;
            var ctx = lienzo.getContext('2d');
            ctx.fillStyle = '#ffffff'; // el JPEG no tiene transparencia
            ctx.fillRect(0, 0, w, h);
            ctx.drawImage(img, 0, 0, w, h);
            URL.revokeObjectURL(direccion);
            var datos = lienzo.toDataURL('image/jpeg', CALIDAD);
            var campoArchivo = oculto(campo, 'fotoArchivo');
            var campoQuitar = oculto(campo, 'quitarFoto');
            var enlace = enlaceInput(campo);
            if (campoArchivo) {
                campoArchivo.value = datos;
            }
            if (campoQuitar) {
                campoQuitar.value = 'false';
            }
            if (enlace) {
                enlace.value = ''; // o es una foto, o es un enlace
            }
            campo.setAttribute('data-listo', 'si');
            aviso(campo, '');
            mostrar(campo, datos);
        };
        img.onerror = function () {
            URL.revokeObjectURL(direccion);
            aviso(campo, 'No se pudo leer esa imagen. Prueba con otra.');
        };
        img.src = direccion;
    }

    function quitar(campo) {
        var campoArchivo = oculto(campo, 'fotoArchivo');
        var campoQuitar = oculto(campo, 'quitarFoto');
        var enlace = enlaceInput(campo);
        if (campoArchivo) {
            campoArchivo.value = '';
        }
        if (campoQuitar) {
            campoQuitar.value = 'true';
        }
        if (enlace) {
            enlace.value = '';
        }
        campo.removeAttribute('data-listo');
        aviso(campo, '');
        mostrar(campo, '');
    }

    document.addEventListener('change', function (e) {
        var campo = campoDe(e.target);
        if (!campo) {
            return;
        }
        if (e.target.matches('input[type=file]')) {
            procesar(campo, e.target.files && e.target.files[0]);
            e.target.value = '';
        } else if (e.target.matches('.mj-foto-enlace input, input.mj-foto-enlace')) {
            // Escribió un enlace: se descarta la foto elegida y se muestra la del enlace
            var campoArchivo = oculto(campo, 'fotoArchivo');
            var campoQuitar = oculto(campo, 'quitarFoto');
            if (campoArchivo) {
                campoArchivo.value = '';
            }
            if (campoQuitar) {
                campoQuitar.value = 'false';
            }
            var valor = (e.target.value || '').trim();
            aviso(campo, '');
            if (/^https?:\/\/\S+$/i.test(valor)) {
                mostrar(campo, valor);
            } else {
                mostrar(campo, '');
            }
        }
    });

    document.addEventListener('click', function (e) {
        if (e.target.closest && e.target.closest('.mj-foto-quitar')) {
            var campo = campoDe(e.target);
            if (campo) {
                e.preventDefault();
                quitar(campo);
            }
        }
    });

    ['dragenter', 'dragover'].forEach(function (nombre) {
        document.addEventListener(nombre, function (e) {
            var campo = campoDe(e.target);
            if (campo) {
                e.preventDefault();
                campo.classList.add('arrastrando');
            }
        });
    });
    ['dragleave', 'drop'].forEach(function (nombre) {
        document.addEventListener(nombre, function (e) {
            var campo = campoDe(e.target);
            if (campo) {
                campo.classList.remove('arrastrando');
            }
        });
    });
    document.addEventListener('drop', function (e) {
        var campo = campoDe(e.target);
        if (campo) {
            e.preventDefault();
            procesar(campo, e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files[0]);
        }
    });

    // Pegar una captura o una foto copiada (Ctrl+V) mientras el cursor está en este campo
    document.addEventListener('paste', function (e) {
        var campo = campoDe(document.activeElement) || campoDe(e.target);
        if (!campo || !e.clipboardData || !e.clipboardData.files || !e.clipboardData.files.length) {
            return;
        }
        e.preventDefault();
        procesar(campo, e.clipboardData.files[0]);
    });
})();
