package org.springframework.samples.petclinic.migration;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.visit.Visit;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class TDGHSQL {

    private static String root = "jdbc:hsqldb:mem:petclinic;readonly=true";

    private static Connection hsqldb = init();

    public TDGHSQL(String root) {
        TDGHSQL.root = root;
        TDGHSQL.hsqldb = init();
        TDGHSQL.populate();
    }

    private static void populate() {
        try {
            Forklift.constructDatabase(root, "hsqldb");
            Forklift.fakeData(root);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static Connection init() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(root, "sa", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    public static ResultSet forkliftAllOwners() {
        String sql = "SELECT * FROM owners;";
        return getRecords(sql);
    }

    public static ResultSet forkliftAllTypes() {
        String sql = "SELECT * FROM types;";
        return getRecords(sql);
    }

    public static ResultSet forkliftAllPets() {
        String sql = "SELECT * FROM pets;";
        return getRecords(sql);
    }

    public static ResultSet forkliftAllSpecialties() {
        String sql = "SELECT * FROM specialties;";
        return getRecords(sql);
    }

    public static ResultSet forkliftAllVets() {
        String sql = "SELECT * FROM vets;";
        return getRecords(sql);
    }

    public static ResultSet forkliftAllVetSpecialties() {
        String sql = "SELECT * FROM vet_specialties;";
        return getRecords(sql);
    }

    public static ResultSet forkliftAllVisits() {
        String sql = "SELECT * FROM visits;";
        return getRecords(sql);
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

    public static Pet getPet(String name) {
        ResultSet rs = selectQuery("SELECT * FROM pets WHERE name= '" + name + "';");
        if(rs != null) {
            return createPetFromResultSet(rs);
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

        if(rs != null) {
            try {
                while(rs.next()) {
                    PetType petType = new PetType();
                    petType.setId(id);
                    petType.setName(rs.getString("name"));
                    return petType;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
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

    public static ResultSet getRecords(String sql) {
        Statement stmt;
        try {
            stmt = hsqldb.createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet selectQuery(String s) {
        //System.out.println(s);
        Statement stmt;
        ResultSet rs = null;
        try {
            stmt = hsqldb.createStatement();
            rs    = stmt.executeQuery(s);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

}
