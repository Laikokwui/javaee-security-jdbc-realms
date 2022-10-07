/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import entity.Employee;
import entity.EmployeeDTO;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;

/**
 *
 * @author elau
 */
@DeclareRoles({"ED-APP-ADMIN","ED-APP-USERS"})
@Stateless
public class EmployeeManagement implements EmployeeManagementRemote {

    @EJB
    private EmployeeFacadeLocal employeeFacade;

    private Employee employeeDTO2Entity(EmployeeDTO empDTO) {
        if (empDTO == null) {
            // just in case
            return null;
        }

        String empid = empDTO.getEmpid();
        String name = empDTO.getName();
        String phone = empDTO.getPhone();
        String address = empDTO.getAddress();
        String email = empDTO.getEmail();
        String password = empDTO.getPassword();
        String appGroup = empDTO.getAppGroup();
        String bankAccountId = empDTO.getBnkAccId();
        Double salary = empDTO.getSalary();
        Boolean active = empDTO.isActive();

        Employee employee = new Employee(empid, name, phone, address, email,
                password, appGroup, bankAccountId, salary, active);

        return employee;
    }
    
    private EmployeeDTO employeeEntity2DTO(Employee employee) {
        if (employee == null) {
            // just in case
            return null;
        }
        
        EmployeeDTO empDTO = new EmployeeDTO(
                employee.getEmpid(),
                employee.getName(),
                employee.getPhone(),
                employee.getAddress(),
                employee.getEmail(),
                employee.getPassword(),
                employee.getAppGroup(),
                employee.getBnkAccId(),
                employee.getSalary(),
                employee.isActive()
        );
        
        return empDTO;
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    /**
     * check whether the employee is in the system
     *
     * @param empId
     * @return true if the employee is in the system, false otherwise
     */
    @Override
    @RolesAllowed({"ED-APP-ADMIN", "ED-APP-USERS"})
    public boolean hasEmployee(String empId) {
        return employeeFacade.hasEmployee(empId);
    }

    /**
     * add an employee to the system
     *
     * @param empDTO
     * @return true if addition is successful, false otherwise
     */
    @Override
    @RolesAllowed({"ED-APP-ADMIN"})
    public boolean addEmployee(EmployeeDTO empDTO) {

        if (empDTO == null) {
            // just in case
            return false;
        }


        // check employee exist?
        if (hasEmployee(empDTO.getEmpid())) {
            // employee exists, cannot add one
            return false;
        }

        // employee not exist
        // convert to entity
        Employee emp = this.employeeDTO2Entity(empDTO);
        
        try {
            String password = emp.getPassword();
            String hashNPassword;
            hashNPassword = ConvertToHexString(GenerateSHA(password));
            emp.setPassword(hashNPassword);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EmployeeManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // add one
        return employeeFacade.addEmployee(emp);
    }

    /**
     * update employee details without updating password
     *
     * @param empDTO
     * @return true if update is successful, false otherwise
     */
    @Override
    @RolesAllowed({"ED-APP-ADMIN","ED-APP-USERS"})
    public boolean updateEmpolyeeDetails(EmployeeDTO empDTO) {
        // check employee exist?
        if (!hasEmployee(empDTO.getEmpid())) {
            return false;
        }

        // employee exist, update details
        // convert to entity class
        Employee emp = this.employeeDTO2Entity(empDTO);
        // update details
        return employeeFacade.updateEmployeeDetails(emp);
    }

    /**
     * update employee's password
     *
     * @param empId
     * @param newPassword
     * @return true if update successful, false otherwise
     */
    @Override
    @RolesAllowed({"ED-APP-ADMIN","ED-APP-USERS"})
    public boolean updateEmployeePassword(String empId, String newPassword) {
        try {
            String hashNPassword;
            hashNPassword = ConvertToHexString(GenerateSHA(newPassword));
            return employeeFacade.updatePassword(empId, hashNPassword);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EmployeeManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * get employee details and use a DTO to transmit the details
     *
     * @param empId
     * @return a DTO containing the information of the employee if exists, null
     * otherwise
     */
    @Override
    @RolesAllowed({"ED-APP-ADMIN","ED-APP-USERS"})
    public EmployeeDTO getEmployeeDetails(String empId) {
        // get the employee
        Employee employee = employeeFacade.find(empId);

        if (employee == null) {
            // not found - no such employee
            return null;
        } else {
            // found - prepare details
            EmployeeDTO empDTO = new EmployeeDTO(employee.getEmpid(),
                    employee.getName(), employee.getPhone(), employee.getAddress(),
                    employee.getEmail(), employee.getPassword(), employee.getAppGroup(),
                    employee.getBnkAccId(), employee.getSalary(), employee.isActive());
            
            return empDTO;
        }
    }

    /**
     * set the employee's active status to false
     * 
     * @param empId
     * @return true if it can be set to inactive and have set to inactive; false otherwise
     */
    @Override
    @RolesAllowed({"ED-APP-ADMIN"})
    public boolean deleteEmployee(String empId) {
        return employeeFacade.deleteEmployee(empId);
    }
    
    /**
     * physically remove an employee's record from database
     * 
     * This is for lab purposes - never ever do this in real world applications
     * 
     * @param empId
     * @return true if the employee record has been physically removed from the database, false otherwise 
     */
    @Override
    @RolesAllowed({"ED-APP-ADMIN"})
    public boolean removeEmployee(String empId) {
        return employeeFacade.removeEmployee(empId);
    }
    
    public static byte[] GenerateSHA(String input) throws NoSuchAlgorithmException { // ConvertToHexString(GenerateSHA(password)
        MessageDigest md = MessageDigest.getInstance("SHA-256"); // Static getInstance method is called with hashing SHA
        return md.digest(input.getBytes(StandardCharsets.UTF_8)); // digest() method called to calculate message digest of an input and return array of byte 
    } 
    
    public static String ConvertToHexString(byte[] hash) { 
        BigInteger number = new BigInteger(1, hash); // Convert byte array into signum representation 
        StringBuilder stringBuilder = new StringBuilder(number.toString(16)); // Convert message digest into hex value 
        
        while (stringBuilder.length() < 32) { // Pad with leading zeros 
            stringBuilder.insert(0, '0');  
        }  
        return stringBuilder.toString();  
    } 
}
