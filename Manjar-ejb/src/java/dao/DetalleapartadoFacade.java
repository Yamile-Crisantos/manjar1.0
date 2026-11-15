/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import modelo.Detalleapartado;

/**
 *
 * @author yamil
 */
@Stateless
public class DetalleapartadoFacade extends AbstractFacade<Detalleapartado> {

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DetalleapartadoFacade() {
        super(Detalleapartado.class);
    }
    
}
