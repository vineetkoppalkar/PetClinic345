/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.core.style.ToStringCreator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.model.NamedEntity;
import org.springframework.samples.petclinic.visit.Visit;

/**
 * Simple business object representing a pet.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Entity
@Table(name = "pets")
public class Pet extends NamedEntity {

    @Column(name = "birth_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private PetType type;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "petId", fetch = FetchType.EAGER)
    private Set<Visit> visits;
    
    // Old constructor
    public Pet() {
    	this(new LinkedHashSet<>());
    }
    
    // New parameterized constructor
    public Pet(Set<Visit> visits) {
    	this.visits = visits;
    }

    public Pet(int id, String name, LocalDate birthDate, PetType type, Owner owner) {
        super(id, name);
        this.birthDate = birthDate;
        this.type = type;
        this.owner = owner;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public PetType getType() {
        return this.type;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public Owner getOwner() {
        return this.owner;
    }

    protected void setOwner(Owner owner) {
        this.owner = owner;
    }

    //Code added for new db    
    public void setOwnerTdg(Owner owner) {
    	setOwner(owner);
    }

    protected Set<Visit> getVisitsInternal() {
        if (this.visits == null) {
            this.visits = new HashSet<>();
        }
        return this.visits;
    }

    protected void setVisitsInternal(Set<Visit> visits) {
        this.visits = visits;
    }
    
    //code added for new db
    public void setVisitsTdg(List<Visit> visits) {
    	Set<Visit> visitSet = new HashSet<Visit>(visits);
    	setVisitsInternal(visitSet);
    }

    public List<Visit> getVisits() {
        List<Visit> sortedVisits = new ArrayList<>(getVisitsInternal());
        PropertyComparator.sort(sortedVisits,
                new MutableSortDefinition("date", false, false));
        return Collections.unmodifiableList(sortedVisits);
    }

    public void addVisit(Visit visit) {
        getVisitsInternal().add(visit);
        visit.setPetId(this.getId());
    }

    public String displayInfo() {
        return new ToStringCreator(this)
            .append("id", this.getId())
            .append("name", this.getName())
            .append("birth_date", this.getBirthDate().toString())
            .append("type_id", this.getType().getId())
            .append("owner_id", this.getOwner().getId()).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if(obj == null || obj.getClass()!= this.getClass())
            return false;

        Pet pet = (Pet) obj;

        if (this.getId() == null || !this.getId().equals(pet.getId()))
            return false;

        if (this.getName() == null || !this.getName().equals(pet.getName()))
            return false;

        if (this.getBirthDate() == null || !this.getBirthDate().equals(pet.getBirthDate()))
            return false;

        if (this.getType() == null || !this.getType().getId().equals(pet.getType().getId()))
            return false;

        if (this.getOwner() == null || !this.getOwner().equals(pet.getOwner()))
            return false;

        return true;
    }
}
