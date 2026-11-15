/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import modelo.Detallecomanda;

/**
 *
 * @author yamil
 */
@Stateless
public class DetallecomandaFacade extends AbstractFacade<Detallecomanda> {

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DetallecomandaFacade() {
        super(Detallecomanda.class);
    }
    
}
