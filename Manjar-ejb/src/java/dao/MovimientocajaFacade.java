/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import modelo.Movimientocaja;

/**
 *
 * @author yamil
 */
@Stateless
public class MovimientocajaFacade extends AbstractFacade<Movimientocaja> {

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MovimientocajaFacade() {
        super(Movimientocaja.class);
    }
    
}
