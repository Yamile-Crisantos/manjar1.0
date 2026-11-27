/*
 * Pantallas con carta + pedido (comandas del mesero y apartar del cliente): todo lo que no necesita al servidor,
 * para que sea instantáneo.
 *   - Pestañas "Tomar pedido" / "Comandas".
 *   - Buscador y categorías de la carta (se filtra lo que ya está en la página).
 *   - En el celular, el pedido es una barra inferior que se abre y se cierra.
 * La carta y las pestañas se vuelven a dibujar con cada actualización (AJAX); aquí se vuelve a aplicar lo que
 * el usuario tenía elegido (búsqueda, categoría, pestaña) en cuanto cambia la página.
 */
var mjPos = (function () {
    var cat = '*';
    var pendiente = false;

    function raiz() {
        return document.getElementById('mjPos');
    }

    function norm(t) {
        return (t || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();
    }

    function buscador() {
        return document.getElementById('mjBuscar');
    }

    function filtrar() {
        var r = raiz();
        if (!r) {
            return;
        }
        var q = norm(buscador() ? buscador().value : '');
        var chips = r.querySelectorAll('.mj-chip');
        // Si la categoría elegida ya no existe (porque se agotó todo lo de esa categoría), se vuelve a "Todos"
        if (cat !== '*') {
            var existe = false;
            chips.forEach(function (c) {
                if (c.getAttribute('data-cat') === cat) {
                    existe = true;
                }
            });
            if (!existe) {
                cat = '*';
            }
        }
        var total = 0;
        var grupos = r.querySelectorAll('.mj-carta-grupo');
        grupos.forEach(function (g) {
            var delGrupo = cat === '*' || g.getAttribute('data-cat') === cat;
            var visibles = 0;
            g.querySelectorAll('.mj-plato').forEach(function (p) {
                var ok = delGrupo && (!q || norm(p.textContent).indexOf(q) >= 0);
                p.classList.toggle('oculto', !ok);
                if (ok) {
                    visibles++;
                }
            });
            g.classList.toggle('oculto', visibles === 0);
            total += visibles;
        });
        chips.forEach(function (c) {
            c.classList.toggle('activo', c.getAttribute('data-cat') === cat);
        });
        var sin = r.querySelector('.mj-sin-resultados');
        if (sin) {
            sin.classList.toggle('oculto', total > 0 || grupos.length === 0);
        }
    }

    function pestanas() {
        var r = raiz();
        if (!r) {
            return;
        }
        var vista = r.getAttribute('data-vista') || 'pedido';
        r.querySelectorAll('.mj-pos-tab').forEach(function (t) {
            var activa = t.getAttribute('data-vista') === vista;
            t.classList.toggle('activa', activa);
            t.setAttribute('aria-pressed', activa ? 'true' : 'false');
        });
    }

    function accesibilidad() {
        var res = document.querySelector('.mj-pedido-resumen');
        if (res && !res.hasAttribute('role')) {
            res.setAttribute('role', 'button');
            res.setAttribute('tabindex', '0');
            res.setAttribute('aria-label', 'Abrir o cerrar el pedido');
        }
    }

    function sincronizar() {
        pendiente = false;
        filtrar();
        pestanas();
        accesibilidad();
    }

    function programar() {
        if (!pendiente) {
            pendiente = true;
            window.requestAnimationFrame(sincronizar);
        }
    }

    function carrito() {
        return document.getElementById('mjCarrito');
    }

    function alternarPedido() {
        var c = carrito();
        if (c) {
            c.classList.toggle('abierto');
        }
    }

    function cerrarPedido() {
        var c = carrito();
        if (c) {
            c.classList.remove('abierto');
        }
    }

    document.addEventListener('click', function (e) {
        var t = e.target;
        if (!t || !t.closest) {
            return;
        }
        var tab = t.closest('.mj-pos-tab');
        if (tab) {
            var r = raiz();
            r.setAttribute('data-vista', tab.getAttribute('data-vista'));
            pestanas();
            window.scrollTo(0, 0);
            return;
        }
        var chip = t.closest('.mj-chip');
        if (chip) {
            cat = chip.getAttribute('data-cat');
            filtrar();
            return;
        }
        if (t.closest('.mj-pedido-resumen')) {
            alternarPedido();
            return;
        }
        // Un toque fuera del pedido abierto lo cierra (solo importa en el celular)
        var c = carrito();
        if (c && c.classList.contains('abierto') && !c.contains(t) && !t.closest('.ui-growl, .ui-dialog, .ui-widget-overlay')) {
            cerrarPedido();
        }
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            cerrarPedido();
        } else if ((e.key === 'Enter' || e.key === ' ') && e.target && e.target.classList
                && e.target.classList.contains('mj-pedido-resumen')) {
            e.preventDefault();
            alternarPedido();
        }
    });

    document.addEventListener('input', function (e) {
        if (e.target && e.target.id === 'mjBuscar') {
            filtrar();
        }
    });

    document.addEventListener('DOMContentLoaded', function () {
        var r = raiz();
        if (!r) {
            return;
        }
        sincronizar();
        // Cada actualización de la carta o de las pestañas vuelve a aplicar la búsqueda, la categoría y la pestaña
        new MutationObserver(programar).observe(r, {childList: true, subtree: true});
    });

    /** Cambia de pestaña desde el servidor (por ejemplo, al crear un apartado se muestra "Mis apartados"). */
    function mostrar(vista) {
        var r = raiz();
        if (r) {
            r.setAttribute('data-vista', vista);
            pestanas();
            window.scrollTo(0, 0);
        }
    }

    return {cerrarPedido: cerrarPedido, filtrar: filtrar, mostrar: mostrar};
})();
