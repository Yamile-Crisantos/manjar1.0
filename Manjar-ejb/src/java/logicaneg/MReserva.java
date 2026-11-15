/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB30/StatelessEjbClass.java to edit this template
 */
package logicaneg;

import dao.DetalleapartadoFacade;
import dao.PagoFacade;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;

/**
 *
 * @author yamil
 */
@Stateless
@LocalBean
public class MReserva implements Serializable {

    @EJB
    private DetalleapartadoFacade detalleapartadoFacade;
    //con este podemos hacer manipulaciones vamos a poder listar elementos ejemplo:
    /**
     public List<Tutor> getTutores()
     {
     return (List<Tutor> tutor.findAll());
      }
     */
    // Se pueden hacer diferente consultas

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
}
