package org.springframework.samples.petclinic.migration;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
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
        System.out.println(s);
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
        System.out.println(s);
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
                    Pet pet = new Pet();
                    pet.setId(rs.getInt(7));
                    pet.setName(rs.getString("name"));
                    pet.setBirthDate(LocalDate.parse(rs.getString("birth_date")));
                    pet.setType(getPetType(rs.getInt("type_id")));
                    pet.setOwnerTdg(owner);
                    owner.addPet(pet);
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

    public static Collection<Owner> getOwnersByLastName(String lastName){
        Collection<Owner> owners = new ArrayList<Owner>();
        if(lastName.equals("")){
            ResultSet rs = selectQuery("SELECT * FROM owners;");
            if(rs != null){
                try {
                    while (rs.next()) {
                        Owner owner = new Owner();
                        owner.setId(rs.getInt("id"));
                        owner.setFirstName(rs.getString("first_name"));
                        owner.setLastName(rs.getString("last_name"));
                        owner.setAddress(rs.getString("address"));
                        owner.setCity(rs.getString("city"));
                        owner.setTelephone(rs.getString("telephone"));
                        rs = selectQuery("SELECT name FROM pets WHERE owner_id = " + String.valueOf(rs.getInt("id")) + ";");
                        ArrayList<String> petName = new ArrayList<String>();
                        if(rs != null) {
                            while(rs.next()) {
                                petName.add(rs.getString("name"));
                            }
                            for(String ownerPetName: petName){
                                owner.addPet(getPet(ownerPetName));
                            }
                        }
                        owners.add(owner);
                    }
                }
                catch (SQLException e){
                    e.printStackTrace();
                }
                return owners;
            }
            return null;
        }

        ResultSet rs = selectQuery("SELECT * FROM owners WHERE last_name=" + lastName +";");
        if(rs != null){
            try {
                while (rs.next()) {
                    Owner owner = new Owner();
                    owner.setId(rs.getInt("id"));
                    owner.setFirstName(rs.getString("first_name"));
                    owner.setLastName(rs.getString("last_name"));
                    owner.setAddress(rs.getString("address"));
                    owner.setCity(rs.getString("city"));
                    owner.setTelephone(rs.getString("telephone"));
                    rs = selectQuery("SELECT name FROM pets WHERE owner_id=" + String.valueOf(rs.getInt("id")) + ";");
                    ArrayList<String> petId = new ArrayList<String>();
                    if (rs != null) {
                        while (rs.next()) {
                            petId.add(rs.getString("name"));
                        }
                        for (String ownerPetId : petId) {
                            owner.addPet(getPet(ownerPetId));
                        }
                    }
                    owners.add(owner);
                }
            }
            catch(SQLException e){
                e.printStackTrace();
            }
            return owners;
        }
        return null;
    }

    public static List<Vet> getAllVets(){
        ResultSet rs = selectQuery("SELECT * FROM vets;");
        if(rs != null){
            try{
                List<Vet> vets = new ArrayList<Vet>();
                while(rs.next()){
                    Vet vet = new Vet();
                    vet.setId(rs.getInt("id"));
                    vet.setFirstName(rs.getString("first_name"));
                    vet.setLastName(rs.getString("last_name"));
                    //specialties
                    List<Specialty> vetSpecialties = getVetSpecialties(rs.getInt("id"));
                    if(vetSpecialties != null){
                        for(Specialty specialty: vetSpecialties){
                            vet.addSpecialty(specialty);
                        }
                    }

                }
                return vets;
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<Specialty> getVetSpecialties(Integer vetId){
        ResultSet rs = selectQuery("SELECT * FROM vet_specialties WHERE vet_id=" + String.valueOf(vetId) +";");
        if(rs != null){
            try{
                List<Specialty> specialties = new ArrayList<Specialty>();
                while(rs.next()){
                    ResultSet bs = selectQuery("SELECT * FROM specialties WHERE id = " + String.valueOf(rs.getInt("id")) + ";");
                    Specialty specialty = new Specialty();
                    specialty.setName(bs.getString("name"));
                    specialty.setId(bs.getInt("id"));
                    specialties.add(specialty);
                }
                return specialties;
            }
            catch (SQLException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void addVet(String firstName, String lastName) {
        insertQuery("INSERT INTO vets (id, first_name, last_name) VALUES (NULL, '" + firstName + "', '" + lastName + "');");
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
        insertQuery("UPDATE vets SET first_name = '" + firstName + "', last_name = '" + lastName + "' WHERE id = " + String.valueOf(id) + ";");
    }
    
    public static void deleteVet(Integer id) {
        insertQuery("DELETE FROM vets WHERE id=" + String.valueOf(id) + ";");
    }
    
    public static void addSpecialty(String specialty) {
        insertQuery("INSERT INTO specialties (id, name) VALUES (NULL, '" + specialty + "');");
    }
    
    public static void addVetSpecialty(Integer vetId, Integer specialtyId) {
        insertQuery("INSERT INTO vet_specialties (vet_id, specialty_id) VALUES ("+ String.valueOf(vetId) + ", "+ String.valueOf(specialtyId) + ");");
    }
    
    public static void addVisit(Integer id, Integer petId, Date visitDate, String description){
        insertQuery("INSERT INTO visits (id, pet_id, visit_date, description) VALUES (" + String.valueOf(id) + ", " + String.valueOf(petId) +", " + String.valueOf(visitDate) + ", '" + description + "');");
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

    public static Visit getVisit(Integer visitId){
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

    public static void addPetType(String name) {
        insertQuery("INSERT INTO types (id, name) VALUES (NULL, '" + name + "');");
    }
    
    public static void updatePet(Integer id, String name, Date birthDate, Integer typeId, Integer ownerId) {
        insertQuery("UPDATE pets SET  name = '" + name + "', birth_date = '" + birthDate + "', type_id = " + typeId + ", owner_id = " + ownerId + " WHERE id = " + id + ";");
    }
    
    public static void deletePet(Integer id) {
        insertQuery("DELETE FROM pets WHERE id=" + String.valueOf(id) + ";");

    }
    
}
