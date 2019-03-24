package org.springframework.samples.petclinic.migration;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.vet.Specialty;

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

    public static ResultSet insertQuery(String s) {
        Statement stmt;
        ResultSet rs = null;
        try {
            stmt = sqlite.createStatement();
            stmt.execute(s);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    private static void populate() {
        try {
            Forklift.constructDatabase("jdbc:sqlite:test", "sqlite");
            Forklift.fakeData("jdbc:sqlite:test");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void addOwner(String firstName, String lastName, String address, String city, String telephone) {
        insertQuery("INSERT INTO owners (id, first_name, last_name, address, city, telephone) VALUES (NULL, '" + firstName + "', '" + lastName + "', '" + address +
            "', '" + city + "', '" + telephone + "');");
    }

    public static Owner getOwner(Integer id) {
        ResultSet rs = selectQuery("SELECT * FROM owners o LEFT JOIN pets p ON o.id = p.owner_id WHERE o.id =" + id + ";");
        if(rs != null) {
            Owner owner = new Owner();
            try{
                while(rs.next()) {
                    owner.setId(rs.getInt("id"));
                    owner.setFirstName(rs.getString("first_name"));
                    owner.setLastName(rs.getString("last_name"));
                    owner.setAddress(rs.getString("address"));
                    owner.setCity(rs.getString("city"));
                    owner.setTelephone(rs.getString("telephone"));
                    if(rs.getString("name") != null) {
                        Pet pet = new Pet();
                        pet.setId(rs.getInt(7));
                        pet.setName(rs.getString("name"));
                        pet.setBirthDate(LocalDate.parse(rs.getString("birth_date")));
                        pet.setType(getPetType(rs.getInt("type_id")));
                        pet.setOwnerTdg(owner);
                        owner.addPet(pet);
                    }
                }
                return owner;
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<Owner> getAllOwners() {
        List<Owner> results = new ArrayList<>();
        ResultSet rs = selectQuery("SELECT * FROM owners");
        try {
            while (rs.next()) {
                results.add(createOwnerFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    private static Owner createOwnerFromResultSet(ResultSet rs) {
        Owner owner = new Owner();
        if(rs != null) {
            try{
                owner.setId(rs.getInt("id"));
                owner.setFirstName(rs.getString("first_name"));
                owner.setLastName(rs.getString("last_name"));
                owner.setAddress(rs.getString("address"));
                owner.setCity(rs.getString("city"));
                owner.setTelephone(rs.getString("telephone"));
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        return owner;
    }


    public static void updateOwner(Integer id, String firstName, String lastName, String address, String city, String telephone) {
        insertQuery("UPDATE owners SET first_name = '" + firstName + "', last_name = '" + lastName + "', address = '" + address+
            "', city ='" + city + "', telephone ='" + telephone + "' WHERE id = " + String.valueOf(id) + ";");
    }

    public static void deleteOwner(Integer id) {
        insertQuery("DELETE FROM owners WHERE id=" + String.valueOf(id) + ";");
    }


    public static void addVet(String firstName, String lastName) {
        insertQuery("INSERT INTO vets (id, first_name, last_name) VALUES (NULL, '" + firstName + "', '" + lastName + "');");
    }
    
    public static Vet getVet(Integer id) {
    	ResultSet rs = selectQuery("SELECT * FROM vets WHERE id=" + String.valueOf(id) + ";");
        Vet vet = null;
    	if(rs != null) {
    		try{
    		    while(rs.next()) {
                    vet = new Vet();
                    vet.setId(rs.getInt("id"));
                    vet.setFirstName(rs.getString("first_name"));
                    vet.setLastName(rs.getString("last_name"));
                }
    		rs = selectQuery("SELECT specialty_id FROM vet_specialties WHERE vet_id=" + String.valueOf(id) + ";");
    		if(rs != null) {
                ResultSet specialty = null;
                String vetSpecialtyId = null;
    		    while(rs.next()) {
                    vetSpecialtyId = String.valueOf(rs.getInt("specialty_id"));
                }
                specialty = selectQuery("SELECT name FROM specialties WHERE id=" + vetSpecialtyId + ";");
    			if(specialty != null) {
    			    while(specialty.next()) {
                        Specialty vetSpecialty = new Specialty();
                        vetSpecialty.setId(Integer.parseInt(vetSpecialtyId));
                        vetSpecialty.setName(specialty.getString("name"));
                        vet.addSpecialty(vetSpecialty);
                    }
    			}
    		}		
    		return vet;
    		}catch(SQLException e){
    			e.printStackTrace();
    		}
    	}
    	return null;
    }

    public static List<Vet> getAllVetsConsistencyChecker() {
        List<Vet> results = new ArrayList<>();
        ResultSet rs = selectQuery("SELECT * FROM vets");
        try {
            while (rs.next()) {
                results.add(createVetFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    private static Vet createVetFromResultSet(ResultSet rs) {
        Vet vet = new Vet();
        try {
            vet.setId(rs.getInt("id"));
            vet.setFirstName(rs.getString("first_name"));
            vet.setLastName(rs.getString("last_name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vet;
    }
    
    public static void updateVet(Integer id, String firstName, String lastName) {
        insertQuery("UPDATE vets SET first_name = '" + firstName + "', last_name = '" + lastName + "' WHERE id = " + String.valueOf(id) + ";");
    }
    
    public static void deleteVet(Integer id) {
        insertQuery("DELETE FROM vets WHERE id=" + String.valueOf(id) + ";");
    }
    
    public static void addSpecialty(String specialty) {
        insertQuery("INSERT INTO specialties (id, name) VALUES (NULL, '" + specialty + "');");
    }

    public static void updatedSpecialty(Integer id, String specialty) {
        insertQuery("UPDATE specialties SET name = '" + specialty + "' WHERE id = " + id + ";");
    }

    public static List<Specialty> getAllSpecialties() {
        ResultSet rs = selectQuery("SELECT * FROM specialties");
        List<Specialty> results = new ArrayList<>();
        if(rs != null){
            try{
                while(rs.next()){
                    results.add(createSpecialityFromResultSet(rs));
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return results;
    }

    private static Specialty createSpecialityFromResultSet(ResultSet rs) {
        Specialty specialty = new Specialty();
        try {
            specialty.setId(rs.getInt("id"));
            specialty.setName(rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return specialty;
    }
    
    public static void addVetSpecialty(Integer vetId, Integer specialtyId) {
        insertQuery("INSERT INTO vet_specialties (vet_id, specialty_id) VALUES ("+ String.valueOf(vetId) + ", "+ String.valueOf(specialtyId) + ");");
    }
    
    public static void addVisit(Integer id, Integer petId, Date visitDate, String description){
        insertQuery("INSERT INTO visits (id, pet_id, visit_date, description) VALUES (" + String.valueOf(id) + ", " + String.valueOf(petId) +", DATE('" + visitDate + "'), '" + description + "');");
    }
    
    public static List<Visit> getVisits(Integer petId){
    	ResultSet rs = selectQuery("SELECT * FROM visits where pet_id=" + String.valueOf(petId) + ";");
    	if(rs != null){
    		try{
    		List<Visit> visits = new ArrayList<Visit>();
    		while(rs.next()){
                visits.add(createVisitFromResultSet(rs));
    		}
    		return visits;
    		}catch (SQLException e){
    			e.printStackTrace();
    		}
    	}
    	return null;
    }

    public static Visit getVisit(Integer visitId){
        ResultSet rs = selectQuery("SELECT * FROM visits where id=" + String.valueOf(visitId) + ";");
        if(rs != null){
            return createVisitFromResultSet(rs);
        }
        return null;
    }

    public static List<Visit> getAllVisits(){
        ResultSet rs = selectQuery("SELECT * FROM visits");
        List<Visit> visits = new ArrayList<>();
        if(rs != null){
            try{
                while(rs.next()){
                    visits.add(createVisitFromResultSet(rs));
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return visits;
    }

    private static Visit createVisitFromResultSet(ResultSet rs){
        Visit visit = new Visit();
        try {
            visit.setDate(LocalDate.parse(rs.getString("visit_date")));
            visit.setId(rs.getInt("id"));
            visit.setPetId(rs.getInt("pet_id"));
            visit.setDescription(rs.getString("description"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return visit;
    }

    public static void updateVisit(Integer id, Integer petId, Date visitDate, String description){
        insertQuery("UPDATE visits SET pet_id = " + String.valueOf(petId) + ", visit_date = '" + String.valueOf(visitDate) + "', description = '" + description + "' WHERE id = " + String.valueOf(id) + ";");
    }
    
    public static void deleteVisit(Integer id) {
        insertQuery("DELETE FROM visits WHERE id=" + String.valueOf(id) + ";");
    }
    
    public static void addPet(String name, Date birthDate, Integer typeId, Integer ownerId) {
        insertQuery("INSERT INTO pets (id, name, birth_date, type_id, owner_id) VALUES (NULL, '" + name + "', '" + birthDate + "', " + typeId + ", " + String.valueOf(ownerId) + ");");
    }
    
    public static Pet getPet(String name) {
    	ResultSet rs = selectQuery("SELECT * FROM pets WHERE name= '" + name + "';");
    	if(rs != null) {
    		try {
    			Pet pet = new Pet();
				pet.setId(rs.getInt("id"));
				pet.setName(rs.getString("name"));
				pet.setBirthDate(LocalDate.parse(rs.getString("birth_date")));
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

    public static List<Pet> getAllPets() {
        List<Pet> results = new ArrayList<>();
        ResultSet rs = selectQuery("SELECT * FROM pets");
        try {
            while (rs.next()) {
                results.add(createPetFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    private static Pet createPetFromResultSet(ResultSet rs) {
        Pet pet = new Pet();
        if(rs != null) {
            try{
                pet.setId(rs.getInt("id"));
                pet.setName(rs.getString("name"));
                pet.setBirthDate(LocalDate.parse(rs.getString("birth_date")));
                pet.setType(getPetType(rs.getInt("type_id")));
                pet.setOwnerTdg(getOwner(rs.getInt("owner_id")));
                pet.setVisitsTdg(getVisits(rs.getInt("id")));
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        return pet;
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

    public static List<PetType> getAllTypes() {
        List<PetType> results = new ArrayList<>();
        ResultSet rs = selectQuery("SELECT * FROM types");
        try {
            while (rs.next()) {
                results.add(createPetTypeFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    private static PetType createPetTypeFromResultSet(ResultSet rs) {
        PetType petType = new PetType();
        if(rs != null) {
            try{
                petType.setId(rs.getInt("id"));
                petType.setName(rs.getString("name"));
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        return petType;
    }

    public static void addPetType(String name) {
        insertQuery("INSERT INTO types (id, name) VALUES (NULL, '" + name + "');");
    }

    public static void updatePetType(Integer id, String name) {
        insertQuery("UPDATE types SET name = '" + name + "' WHERE id = " + id + ";");
    }
    
    public static void updatePet(Integer id, String name, Date birthDate, Integer typeId, Integer ownerId) {
        insertQuery("UPDATE pets SET  name = '" + name + "', birth_date = '" + birthDate + "', type_id = " + typeId + ", owner_id = " + ownerId + " WHERE id = " + id + ";");
    }
    
    public static void deletePet(Integer id) {
        insertQuery("DELETE FROM pets WHERE id=" + String.valueOf(id) + ";");

    }
    
}
