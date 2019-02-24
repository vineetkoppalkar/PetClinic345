package org.springframework.samples.petclinic.owner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link PetTypeFormatter}
 *
 * @author Colin But
 */
@RunWith(MockitoJUnitRunner.class)
public class PetTypeFormatterTests {

    @Mock
    private PetRepository pets;

    private PetTypeFormatter petTypeFormatter;

    private List<PetType> petTypes = new ArrayList<>();

    @Before
    public void setup() {
        this.petTypeFormatter = new PetTypeFormatter(pets);
        PetType dog = new PetType();
        dog.setName("Dog");
        PetType bird = new PetType();
        bird.setName("Bird");
        petTypes.add(dog);
        petTypes.add(bird);
    }

    @Test
    public void testPrint() {
        PetType petType = new PetType();
        petType.setName("Hamster");
        String petTypeName = this.petTypeFormatter.print(petType, Locale.ENGLISH);
        assertEquals("Hamster", petTypeName);
    }

    @Test
    public void shouldParse() throws ParseException {
        Mockito.when(this.pets.findPetTypes()).thenReturn(petTypes);
        PetType petType = petTypeFormatter.parse("Bird", Locale.ENGLISH);
        assertEquals("Bird", petType.getName());
    }

    @Test(expected = ParseException.class)
    public void shouldThrowParseException() throws ParseException {
        Mockito.when(this.pets.findPetTypes()).thenReturn(petTypes);
        petTypeFormatter.parse("Fish", Locale.ENGLISH);
    }

}
