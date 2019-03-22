package org.springframework.samples.petclinic.migration;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.vet.Specialty;
import java.time.LocalDate;

import org.springframework.samples.petclinic.migration.Forklift;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.visit.Visit;

public class TDGSQLite {

    private static String root = "jdbc:sqlite:memory";
    private static Connection sqlite = init();

    public TDGSQLite(String root) {
        TDGSQLite.root = root;
        TDGSQLite.sqlite = init();
        TDGSQLite.populate();
    }

    private static Connection init() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(root);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    public static ResultSet selectQuery(String s) {

        Statement stmt;
        ResultSet rs = null;
        try {
            stmt = sqlite.createStatement();
            rs    = stmt.executeQuery(s);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    private static void populate() {
        try {
            Forklift.constructDatabase("jdbc:sqlite:test");
            Forklift.forkliftDatabase("jdbc:sqlite:test");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

    }

    public static void addOwner(String firstName, String lastName, String address, String city, String telephone) {
        selectQuery("INSERT INTO owners id, first_name, last_name VALUES (NULL, " + firstName + ", " + lastName + ", " + address +
            ", " + city + ", " + telephone + ");");
    }

    public static Owner getOwner(Integer id) {
        ResultSet rs = selectQuery("SELECT * FROM owners WHERE id=" + String.valueOf(id) + ";");
        if(rs != null) {
            try{
                Owner owner = new Owner();
                owner.setId(rs.getInt("id"));
                owner.setFirstName(rs.getString("first_name"));
                owner.setLastName(rs.getString("last_name"));
                owner.setAddress(rs.getString("address"));
                owner.setCity(rs.getString("city"));
                owner.setTelephone(rs.getString("telephone"));
                rs = selectQuery("SELECT name FROM pets WHERE owner_id=" + String.valueOf(id) + ";");
                ArrayList<String> petId = new ArrayList();
                if(rs != null) {
                    while(rs.next()) {
                        petId.add(rs.getString("name"));
                    }
                    for(String ownerPetId: petId){
                        owner.addPet(getPet(ownerPetId));
                    }
                }
                return owner;
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void updateOwner(Integer id, String firstName, String lastName, String address, String city, String telephone) {
        selectQuery("UPDATE owners SET first_name = " + firstName + ", last_name = " + lastName + ", address = " + address+
            ", city =" + city + ", telephone" + telephone + " WHERE id = " + String.valueOf(id) + ";");
    }

    public static void deleteOwner(Integer id) {
        selectQuery("DELETE FROM owners WHERE id=" + String.valueOf(id) + ";");
    }


    public static void addVet(String firstName, String lastName) {
    	selectQuery("INSERT INTO vets id, first_name, last_name VALUES (NULL, " + firstName + ", " + lastName + ");");
    }
    
    public static Vet getVet(Integer id) {
    	ResultSet rs = selectQuery("SELECT * FROM vets WHERE id=" + String.valueOf(id) + ";");
    	if(rs != null) {
    		try{
    		Vet vet = new Vet();
    		vet.setId(rs.getInt("id"));
    		vet.setFirstName(rs.getString("first_name"));
    		vet.setLastName(rs.getString("last_name"));
    		rs = selectQuery("SELECT specialty_id FROM vet_specialties WHERE vet_id=" + String.valueOf(id) + ";");
    		if(rs != null) {
    			ResultSet specialty = selectQuery("SELECT name FROM specialties WHERE id=" + String.valueOf(rs.getInt("specialty_id")) +";");
    			if(specialty != null) {
    				Specialty vetSpecialty = new Specialty();
    				vetSpecialty.setId(rs.getInt("specialty_id"));
    				vetSpecialty.setName(specialty.getString("name"));
    				vet.addSpecialty(vetSpecialty);
    			}
    		}		
    		return vet;
    		}catch(SQLException e){
    			e.printStackTrace();
    		}
    	}
    	return null;
    }
    
    public static void updateVet(Integer id, String firstName, String lastName) {
    	selectQuery("UPDATE vets SET first_name = " + firstName + ", last_name = " + lastName + " WHERE id = " + String.valueOf(id) + ";");
    }
    
    public static void deleteVet(Integer id) {
    	selectQuery("DELETE FROM vets WHERE id=" + String.valueOf(id) + ";");
    }
    
    public static void addSpecialty(String specialty) {
    	selectQuery("INSERT INTO specialties id, name VALUES (NULL, " + specialty + ");");
    }
    
    public static void addVetSpecialty(Integer vetId, Integer specialtyId) {
    	selectQuery("INSERT INTO vet_specialties vet_id, specialty_id VALUES ("+ String.valueOf(vetId) + ", "+ String.valueOf(specialtyId) + ");");
    }
    
    public static void addVisit(Integer id, Integer petId, Date visitDate, String description){
    	selectQuery("INSERT INTO visits id, pet_id, visit_date, description VALUES (" + String.valueOf(id) + ", " + String.valueOf(petId) +", " + String.valueOf(visitDate) + ", " + description + ");");
    }
    
    public static List<Visit> getVisits(Integer petId){
    	ResultSet rs = selectQuery("SELECT * FROM visits where pet_id=" + String.valueOf(petId) + ";");
    	if(rs != null){
    		try{
    		List<Visit> visits = new ArrayList<Visit>();
    		while(rs.next()){
    			Visit visit = new Visit();
    			
    			visit.setDate(rs.getDate("visit_date").toLocalDate());
    			visit.setId(rs.getInt("id"));
    			visit.setPetId(rs.getInt("pet_id"));
    			visit.setDescription(rs.getString("description"));
    			
    			visits.add(visit);
    		}
    		return visits;
    		}catch (SQLException e){
    			e.printStackTrace();
    		}
    	}
    	return null;
    }

    public static Visit getVisit(Intger visitId){
        ResultSet rs = selectQuery("SELECT * FROM visits where id=" + String.valueOf(visitId) + ";");
        if(rs != null){
            try{
                Visit visit = new Visit();

                visit.setDate(rs.getDate("visit_date").toLocalDate());
    			visit.setId(rs.getInt("id"));
    			visit.setPetId(rs.getInt("pet_id"));
    			visit.setDescription(rs.getString("description"));

                return visit;
            }catch (SQLException e){
    			e.printStackTrace();
    		}
        }
        return null;
    }
    
    public static void updateVisit(Integer id, Integer petId, Date visitDate, String description){
    	selectQuery("UPDATE visits SET pet_id = " + String.valueOf(petId) + ", visit_date = " + String.valueOf(visitDate) + ", description = " + description + "WHERE id = " + String.valueOf(id) + ";");
    }
    
    public static void deleteVisit(Integer id) {
    	selectQuery("DELETE FROM visits WHERE id=" + String.valueOf(id) + ";");
    }
    
    public static void addPet(String name, Date birthDate, Integer typeId, Integer ownerId) {
    	selectQuery("INSERT INTO pets id, name, birth_date, type_id, owner_id VALUES (NULL, " + name + ", " + String.valueOf(birthDate) + ", " + String.valueOf(typeId) + ", " + String.valueOf(ownerId) + ");");
    }
    
    public static Pet getPet(String name) {
    	ResultSet rs = selectQuery("SELECT * FROM pets WHERE name=" + name + ";");
    	if(rs != null) {
    		try {
    			Pet pet = new Pet();
				pet.setName(rs.getString("name"));
				pet.setBirthDate(rs.getDate("birth_date").toLocalDate());
				PetType petType = getPetType(rs.getInt("type_id"));
				pet.setType(petType);
				pet.setOwnerTdg(getOwner(rs.getInt("owner_id")));
				pet.setVisitsTdg(getVisits(rs.getInt("id")));
				return pet;
			} catch (SQLException e) {
					e.printStackTrace();
			}
    	}
    	return null;
    }
    
    public static PetType getPetType(Integer id) {
    	ResultSet rs = selectQuery("SELECT name FROM types WHERE id=" + id + ";");
    	try {
    		if(rs != null) {
				PetType petType = new PetType();
				petType.setId(id);
				petType.setName(rs.getString("name"));
				return petType;
    		}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public static void updatePet(Integer id, String name, Date birthDate, Integer typeId, Integer ownerId) {
    	selectQuery("UPDATE pets SET  name = " + name + ", birth_date = " + birthDate + ", type_id = " + String.valueOf(typeId) + ", owner_id = " + String.valueOf(ownerId) + " WHERE id = " + String.valueOf(id) + ";");
    }
    
    public static void deletePet(Integer id) {
        selectQuery("DELETE FROM pets WHERE id=" + String.valueOf(id) + ";");

    }
    
}
