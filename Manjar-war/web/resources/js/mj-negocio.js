/*
 * Mi negocio (negocio.xhtml): vista previa en vivo del menú, selector de color y de estilo.
 * Lo elegido se guarda en campos ocultos (formNegocio:color, :diseno, :tipografia, :esquinas, :fondo);
 * el servidor vuelve a comprobar que sean valores permitidos y valida el contacto.
 */
(function () {
    var estado = null;   // colores del tema elegido y fondo

    function el(id) {
        return document.getElementById(id);
    }

    function valor(id) {
        var campo = el(id);
        return campo ? campo.value.trim() : '';
    }

    function texto(idPrevia, contenido) {
        var nodo = el(idPrevia);
        if (nodo) {
            nodo.textContent = contenido;
        }
    }

    function mostrar(idPrevia, visible) {
        var nodo = el(idPrevia);
        if (nodo) {
            nodo.style.display = visible ? '' : 'none';
        }
    }

    /** Pone las variables de color de la vista previa (igual que Temas.estilo en el servidor). */
    function aplicarColor() {
        var previa = el('mj-previa');
        if (!previa || !estado) {
            return;
        }
        var oscuro = estado.fondo === 'oscuro';
        previa.style.setProperty('--mj-accent', estado.hex);
        previa.style.setProperty('--mj-primary', estado.hex);
        previa.style.setProperty('--mj-primary-dark', estado.oscuro);
        previa.style.setProperty('--mj-tint', oscuro ? estado.suaveOscuro : estado.suave);
        previa.style.setProperty('--mj-link', oscuro ? estado.claro : estado.oscuro);
        previa.style.setProperty('--mj-dark', estado.profundo);
        previa.style.setProperty('--mj-accent-claro', estado.claro);
    }

    window.mjVistaPrevia = function () {
        texto('mj-prev-nombre', valor('formNegocio:nombre') || 'Tu negocio');
        var lema = valor('formNegocio:lema');
        texto('mj-prev-lema', lema);
        mostrar('mj-prev-lema', lema !== '');

        var aviso = valor('formNegocio:aviso');
        texto('mj-prev-aviso', aviso);

        var horario = valor('formNegocio:horario');
        texto('mj-prev-horario', horario ? 'Horario: ' + horario : '');
        var hay = horario !== '';
        var chips = ['telefono', 'whatsapp', 'instagram', 'facebook'];
        for (var i = 0; i < chips.length; i++) {
            var con = valor('formNegocio:' + chips[i]) !== '';
            mostrar('mj-prev-' + chips[i], con);
            hay = hay || con;
        }
        var cont = el('mj-prev-contacto');
        if (cont) {
            cont.style.display = hay ? '' : 'none';
        }

        var contadores = document.querySelectorAll('.mj-contador[data-de]');
        for (var k = 0; k < contadores.length; k++) {
            var campo = el(contadores[k].getAttribute('data-de'));
            if (campo) {
                contadores[k].textContent = campo.value.length + ' / ' + contadores[k].getAttribute('data-max');
            }
        }
    };

    window.mjElegirColor = function (boton) {
        var campo = el('formNegocio:color');
        if (!campo) {
            return;
        }
        campo.value = boton.getAttribute('data-hex');
        estado.hex = boton.getAttribute('data-hex');
        estado.oscuro = boton.getAttribute('data-oscuro');
        estado.suave = boton.getAttribute('data-suave');
        estado.claro = boton.getAttribute('data-claro');
        estado.profundo = boton.getAttribute('data-profundo');
        estado.suaveOscuro = boton.getAttribute('data-suave-oscuro');
        aplicarColor();
        var todos = document.querySelectorAll('.mj-swatch');
        for (var i = 0; i < todos.length; i++) {
            todos[i].setAttribute('aria-checked', todos[i] === boton ? 'true' : 'false');
        }
    };

    /** Diseño, tipografía, esquinas o fondo: cambia la clase de la vista previa y el campo oculto. */
    window.mjElegirOpcion = function (boton) {
        var grupo = boton.getAttribute('data-grupo');
        var elegido = boton.getAttribute('data-valor');
        var campo = el('formNegocio:' + grupo);
        var previa = el('mj-previa');
        if (!campo || !previa) {
            return;
        }
        campo.value = elegido;
        var prefijo = { diseno: 'mj-diseno-', tipografia: 'mj-tipo-', esquinas: 'mj-esq-', fondo: 'mj-fondo-' }[grupo];
        var clases = previa.className.split(/\s+/).filter(function (c) {
            return c && c.indexOf(prefijo) !== 0;
        });
        clases.push(prefijo + elegido);
        previa.className = clases.join(' ');
        if (grupo === 'fondo') {
            estado.fondo = elegido;
            aplicarColor();
        }
        var hermanos = document.querySelectorAll('.mj-opcion[data-grupo="' + grupo + '"]');
        for (var i = 0; i < hermanos.length; i++) {
            hermanos[i].setAttribute('aria-checked', hermanos[i] === boton ? 'true' : 'false');
        }
    };

    document.addEventListener('DOMContentLoaded', function () {
        var elegido = document.querySelector('.mj-swatch[aria-checked="true"]');
        if (elegido) {
            estado = {
                hex: elegido.getAttribute('data-hex'),
                oscuro: elegido.getAttribute('data-oscuro'),
                suave: elegido.getAttribute('data-suave'),
                claro: elegido.getAttribute('data-claro'),
                profundo: elegido.getAttribute('data-profundo'),
                suaveOscuro: elegido.getAttribute('data-suave-oscuro'),
                fondo: valor('formNegocio:fondo') || 'claro'
            };
        } else {
            estado = { hex: '#C44A24', oscuro: '#992F11', suave: '#F9E4DD', claro: '#E3A58F', profundo: '#4A1C0C', suaveOscuro: '#37150A', fondo: 'claro' };
        }
        var ids = ['nombre', 'lema', 'aviso', 'horario', 'telefono', 'whatsapp', 'instagram', 'facebook'];
        for (var i = 0; i < ids.length; i++) {
            var campo = el('formNegocio:' + ids[i]);
            if (campo) {
                campo.addEventListener('input', window.mjVistaPrevia);
            }
        }
        window.mjVistaPrevia();
    });
})();
