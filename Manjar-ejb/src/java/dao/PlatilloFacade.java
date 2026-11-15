/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import modelo.Platillo;

/**
 *
 * @author yamil
 */
@Stateless
public class PlatilloFacade extends AbstractFacade<Platillo> {

    @PersistenceContext(unitName = "Manjar-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PlatilloFacade() {
        super(Platillo.class);
    }
    
}
