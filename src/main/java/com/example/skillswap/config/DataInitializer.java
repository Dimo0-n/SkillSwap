package com.example.skillswap.config;

import com.example.skillswap.entity.Announce;
import com.example.skillswap.entity.Category;
import com.example.skillswap.repository.AnnounceRepository;
import com.example.skillswap.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private AnnounceRepository announceRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    public DataInitializer(AnnounceRepository announceRepository) {
        this.announceRepository = announceRepository;
    }

    //pentru anunturi
    private byte[] loadImage(String imagePath, int width, int height) {
        try {
            // Încarcă imaginea din calea specificată
            ClassPathResource resource = new ClassPathResource(imagePath);
            InputStream inputStream = resource.getInputStream();

            // Citește imaginea ca BufferedImage
            BufferedImage originalImage = ImageIO.read(inputStream);

            // Redimensionează imaginea la dimensiunea dorită
            BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, width, height, null);
            g.dispose();

            // Convertește imaginea redimensionată într-un array de bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @PostConstruct
    public void init() throws Exception {
        if (announceRepository.count() == 0) {
            addAnnouncements();
        }

        if (categoryRepository.count() == 0) {
            addCategories();
        }
    }


    private void addAnnouncements() throws Exception {

        List<Announce> announceList = List.of(
                new Announce(null, "Ofer lecții de programare Java în schimb de design grafic", "Programez de 5 ani. Caut pe cineva să mă învețe Photoshop.", "Andrei", LocalDateTime.of(2025, 8, 17, 10, 30), "Programare Java", "Design grafic", "", "img/skill/skill-programming.png"),

                new Announce(null, "Învăț engleză în schimb de lecții de chitară", "Profesor de engleză cu experiență. Vreau să învăț chitara.", "Maria", LocalDateTime.of(2025, 8, 16, 14, 15), "Limba engleză", "Chitară", "","img/skill/skill-english.png"),

                new Announce(null, "Ofer cursuri de gătit italian pentru lecții de matematică", "Gătesc preparate italiene. Vreau să-mi îmbunătățesc matematica pentru afacere.", "Ion", LocalDateTime.of(2025, 8, 15, 16, 45), "Gătit italian", "Matematică","", "img/skill/skill-cooking.png"),

                new Announce(null, "Schimb lecții de dans cu reparații auto", "Instructor de salsa și bachata. Vreau să repar mașina singur.", "Carmen", LocalDateTime.of(2025, 8, 14, 11, 20), "Dans", "","Reparații auto", "img/skill/skill-dance.png"),

                new Announce(null, "Ofer servicii de fotografie pentru lecții de yoga", "Fotograf profesionist. Caut instructor de yoga pentru relaxare și sănătate.", "Alex", LocalDateTime.of(2025, 8, 13, 9, 10), "","Fotografie", "Yoga", "img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de chitară în schimb de lecții de germană", "Cânt la chitară de 12 ani. Vreau colaborări internaționale.", "Elena", LocalDateTime.of(2025, 8, 12, 15, 30), "Chitară", "Limba germană", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer lecții de programare web în schimb de fitness", "Developer cu experiență. Vreau să mă întorc în formă.", "Marius", LocalDateTime.of(2025, 8, 11, 18, 0), "Programare web", "Fitness", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de Photoshop în schimb de reparații biciclete", "Expert în Adobe Creative Suite. Bicicleta are nevoie de întreținere.", "Ana", LocalDateTime.of(2025, 8, 10, 12, 45), "Photoshop", "Reparații biciclete", "","img/skill/skill-photoshop.png"),

                new Announce(null, "Ofer cursuri de public speaking pentru lecții de măiestrie", "Antrenez comunicarea de 5 ani. Vreau să lucrez cu lemnul.", "Radu", LocalDateTime.of(2025, 8, 9, 13, 15), "Public speaking", "Măiestrie lemn", "","img/skill/skill-public-speaking.png"),

                new Announce(null, "Ofer cursuri de engleză în schimb de make-up", "Predau engleza la cursuri private. Vreau să învăț make-up profesional.", "Cristina", LocalDateTime.of(2025, 8, 8, 17, 30), "Limba engleză", "Make-up", "","img/skill/skill-english.png"),

                new Announce(null, "Ofer cursuri de gătit pentru traduceri", "Chef cu experiență internațională. Am nevoie de traduceri pentru rețete.", "Sergiu", LocalDateTime.of(2025, 8, 7, 10, 0), "Gătit", "Traduceri", "","img/skill/skill-cooking.png"),

                new Announce(null, "Ofer lecții de dans pentru programare Python", "Predau dans modern. Vreau aplicație pentru studio de dans.", "Gheorghe", LocalDateTime.of(2025, 8, 6, 14, 20), "Dans", "Python", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de fotografie în schimb de vioară", "Fotograf cu 10 ani experiență. Visez să cânt la vioară.", "Mihai", LocalDateTime.of(2025, 8, 5, 16, 10), "Fotografie", "Vioară", "","img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de chitară în schimb de înot", "Cânt chitară clasică și acustică. Vreau să învăț înotul corect.", "Diana", LocalDateTime.of(2025, 8, 4, 11, 50), "Chitară", "Înot", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de public speaking pentru reparații smartphone", "Antrenez prezentarea în public. Telefonul se strică des, vreau ajutor.", "Ioana", LocalDateTime.of(2025, 8, 3, 15, 40), "Public speaking", "Reparații smartphone", "","img/skill/skill-public-speaking.png"),

                new Announce(null, "Ofer cursuri de programare în schimb de design interior", "Programez în Java și Python. Vreau să-mi redecorez apartamentul.", "Vlad", LocalDateTime.of(2025, 8, 2, 19, 25), "Programare", "Design interior", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de Photoshop pentru lecții de skateboard", "Designer grafic cu experiență. La 30 ani vreau skateboard.", "Laura", LocalDateTime.of(2025, 8, 1, 12, 35), "Photoshop", "Skateboard", "","img/skill/skill-photoshop.png"),

                new Announce(null, "Ofer cursuri de dans pentru lecții de croitorie", "Instructor de dans modern. Vreau să-mi croiesc propriile costume.", "Bogdan", LocalDateTime.of(2025, 7, 31, 14, 0), "Dans", "Croitorie", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de gătit pentru întreținere grădină", "Gătesc preparate mediteraneene. Grădina mea are nevoie de îngrijire.", "Roxana", LocalDateTime.of(2025, 7, 30, 16, 15), "Gătit", "Întreținere grădină", "","img/skill/skill-cooking.png"),

                new Announce(null, "Ofer cursuri de chitară pentru programare JavaScript", "Chitaristă cu experiență. Vreau site personal pentru concerte.", "Irina", LocalDateTime.of(2025, 7, 29, 10, 45), "Chitară", "JavaScript", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de fotografie pentru lecții de barbering", "Fotograf de evenimente. Vreau să învăț arta bărbieritului profesional.", "Adrian", LocalDateTime.of(2025, 7, 28, 13, 20), "Fotografie", "Barbering", "","img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de programare pentru origami", "Administrator de rețele IT. Vreau origami pentru relaxare după muncă.", "Cosmin", LocalDateTime.of(2025, 7, 27, 17, 55), "Programare", "Origami", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de public speaking pentru traduceri italiene", "Antrenez public speaking. Vorbesc fluent italiana, pot traduce orice.", "Monica", LocalDateTime.of(2025, 7, 26, 9, 30), "Public speaking", "Traduceri", "","img/skill/skill-public-speaking.png"),

                new Announce(null, "Ofer cursuri de dans pentru editare foto", "Antrenez dans de 6 ani. Vreau Photoshop și Lightroom profesional.", "Daniel", LocalDateTime.of(2025, 7, 25, 18, 40), "Dans", "Editare foto", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de engleză în schimb de surf", "Profesor de engleză la facultate. Plănuiesc vacanță la ocean.", "Silvia", LocalDateTime.of(2025, 7, 24, 11, 10), "Limba engleză", "Surf", "","img/skill/skill-english.png"),

                new Announce(null, "Ofer cursuri de chitară pentru patinaj artistic", "Chitarist cu experiență. Îmi place foarte mult patinajul artistic.", "Omar", LocalDateTime.of(2025, 7, 23, 15, 25), "Chitară", "Patinaj artistic", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de gătit pentru reparații ceasuri", "Gătesc de 15 ani. Am colecție de ceasuri vechi.", "Vasile", LocalDateTime.of(2025, 7, 22, 14, 50), "Gătit", "Reparații ceasuri", "","img/skill/skill-cooking.png"),

                new Announce(null, "Ofer cursuri de fotografie pentru dezvoltare aplicații mobile", "Fotograf montan cu experiență. Vreau aplicații pentru Android și iOS.", "Cătălin", LocalDateTime.of(2025, 7, 21, 16, 35), "Fotografie", "Dezvoltare mobile", "","img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de dans pentru lecții de somelier", "Instructor de arte marțiale. Îmi place vinul, vreau să învăț acorduri.", "Robert", LocalDateTime.of(2025, 7, 20, 12, 15), "Dans", "Somelier", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de programare pentru parkour", "Programator cu master în psihologie. Vreau parkour pentru fitness distractiv.", "Andreea", LocalDateTime.of(2025, 7, 19, 13, 45), "Programare", "Parkour", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de chitară pentru întreținere acvariu", "Chitarist în orchestră. Avariul mare are nevoie de îngrijire profesională.", "Florin", LocalDateTime.of(2025, 7, 18, 17, 20), "Chitară", "Întreținere acvariu", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de public speaking pentru supraviețuire", "Profesor universitar. Vreau tehnici de supraviețuire pentru drumeții montane.", "Gabriela", LocalDateTime.of(2025, 7, 17, 10, 55), "Public speaking", "Supraviețuire", "","img/skill/skill-public-speaking.png"),

                new Announce(null, "Ofer cursuri de engleză pentru breakdance", "Avocat cu experiență. Îmi place foarte mult breakdance-ul urban.", "Lucian", LocalDateTime.of(2025, 7, 16, 14, 30), "Limba engleză", "Breakdance", "","img/skill/skill-english.png"),

                new Announce(null, "Ofer cursuri de gătit pentru apicultură", "Gătesc chinezește autentic. Vreau apicultură ca hobby și miere propriu.", "Wei", LocalDateTime.of(2025, 7, 15, 16, 0), "Gătit", "Apicultură", "","img/skill/skill-cooking.png"),

                new Announce(null, "Ofer cursuri de dans pentru reparații electronice", "Dans profesionist de 12 ani. Aparatele electronice vechi necesită reparații.", "Corina", LocalDateTime.of(2025, 7, 14, 11, 40), "Dans", "Reparații electronice", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de fotografie pentru bucătărie franceză", "Fotograf cu doctorat. Îmi place foarte mult bucătăria franceză autentică.", "Petru", LocalDateTime.of(2025, 7, 13, 15, 10), "Fotografie", "Bucătărie franceză", "","img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de programare pentru scufundări", "Programator certificat. Vreau să explorez lumea subacvatică prin scufundări.", "Priya", LocalDateTime.of(2025, 7, 12, 18, 25), "Programare", "Scufundări", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de chitară pentru pescuit sportiv", "Chitarist cu 8 ani experiență. Vreau pescuit sportiv pentru relaxare.", "Stefan", LocalDateTime.of(2025, 7, 11, 9, 15), "Chitară", "Pescuit sportiv", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de public speaking pentru programare C++", "Profesor de canto cu experiență. Vreau C++ pentru proiecte personale.", "Alina", LocalDateTime.of(2025, 7, 10, 13, 50), "Public speaking", "Programare C++", "","img/skill/skill-public-speaking.png"),

                new Announce(null, "Ofer cursuri de dans pentru meditație", "Instructor de dans cu doctorat. Vreau meditație pentru dezvoltare personală.", "Mircea", LocalDateTime.of(2025, 7, 9, 16, 30), "Dans", "Meditație", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de fotografie pentru design de modă", "Fotograf sportiv în înot. Îmi place moda și designul de ținute.", "Raluca", LocalDateTime.of(2025, 7, 8, 12, 5), "Fotografie", "Design modă", "","img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de gătit pentru căutat ciuperci", "Gătesc cu experiență economică. Vreau să identific ciuperci comestibile.", "Dorin", LocalDateTime.of(2025, 7, 7, 14, 40), "Gătit", "Căutat ciuperci", "","img/skill/skill-cooking.png"),

                new Announce(null, "Ofer cursuri de programare pentru întreținere motociclete", "Programator în trupă rock. Motocicleta veche are nevoie de îngrijire.", "Alex", LocalDateTime.of(2025, 7, 6, 17, 15), "Programare", "Întreținere moto", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de engleză pentru florărie", "Jurnalist cu experiență. Vreau să aranjez flori profesional pentru evenimente.", "Oana", LocalDateTime.of(2025, 7, 5, 10, 20), "Limba engleză", "Florărie", "","img/skill/skill-english.png"),

                new Announce(null, "Ofer cursuri de chitară pentru windsurf", "Chitarist terapeut certificat. Vreau windsurfing pentru vacanțele de vară.", "Rares", LocalDateTime.of(2025, 7, 4, 15, 55), "Chitară", "Windsurf", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de dans pentru stand-up comedy", "Instructor de dans în construcții. Vreau stand-up pentru încredere.", "Ciprian", LocalDateTime.of(2025, 7, 3, 11, 30), "Dans", "Stand-up comedy", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de gătit pentru marketing", "Gătesc profesionist. Vreau să-mi promovez bucătăria cu strategii de marketing.", "Jazz", LocalDateTime.of(2025, 7, 2, 16, 45), "Gătit", "Marketing", "","img/skill/skill-cooking.png"),

                new Announce(null, "Ofer cursuri de fotografie pentru programare R", "Fotograf nutriționist licențiat. Vreau R pentru analize statistice în cercetări.", "Anca", LocalDateTime.of(2025, 7, 1, 13, 10), "Fotografie", "Programare R", "","img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de programare pentru podcasting", "Programator cu experiență. Vreau podcast despre tehnologie și ajutor tehnic.", "Dani", LocalDateTime.of(2025, 6, 30, 18, 0), "Programare", "Podcasting", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de public speaking pentru skateboard", "Public speaking pentru studenți. Vreau skateboard ca activitate de relaxare.", "Meda", LocalDateTime.of(2025, 6, 29, 9, 45), "Public speaking", "Skateboard", "","img/skill/skill-public-speaking.png"),

                new Announce(null, "Ofer cursuri de chitară pentru reparații ceasuri", "Chitarist în orchestră simfonică. Pasiune pentru ceasurile vintage și reparații.", "Cristian", LocalDateTime.of(2025, 6, 28, 14, 25), "Chitară", "Reparații ceasuri","", "img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de engleză pentru supraviețuire urbană", "Profesor de engleză cu doctorat. Vreau supraviețuire pentru situații de criză.", "Teodor", LocalDateTime.of(2025, 6, 27, 16, 50), "Limba engleză", "Supraviețuire urbană", "","img/skill/skill-english.png"),

                new Announce(null, "Ofer cursuri de fotografie pentru fotografie macro", "Fotograf cu certificare internațională. Vreau să fotografiez insecte și flori.", "Luminita", LocalDateTime.of(2025, 6, 26, 12, 35), "Fotografie", "Fotografie macro", "","img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de gătit pentru cross-training", "Gătesc japonez de 2 ani. Vreau să mă întorc în formă.", "Yuki", LocalDateTime.of(2025, 6, 25, 15, 20), "Gătit", "Cross-training", "","img/skill/skill-cooking.png"),

                new Announce(null, "Ofer cursuri de programare pentru consultanță IT", "Programator în orchestră națională. Vreau să-mi modernizez sistemele de acasă.", "Marin", LocalDateTime.of(2025, 6, 24, 17, 10), "Programare", "Consultanță IT", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de dans pentru reparații instrumente", "Instructor de dans certificat. Instrumente muzicale vechi necesită reparații profesionale.", "Sorina", LocalDateTime.of(2025, 6, 23, 10, 0), "Dans", "Reparații instrumente", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de public speaking pentru kitesurfing", "Public speaking pentru limbi clasice. Vreau kitesurfing pentru aventuri pe apă.", "Claudius", LocalDateTime.of(2025, 6, 22, 13, 15), "Public speaking", "Kitesurfing", "","img/skill/skill-public-speaking.png"),

                new Announce(null, "Ofer cursuri de chitară pentru caligrafie", "Chitarist medic veterinar. Vreau caligrafie ca hobby pentru relaxare.", "Carmen", LocalDateTime.of(2025, 6, 21, 16, 40), "Chitară", "Caligrafie", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de fotografie pentru programare Swift", "Fotograf profesionist. Vreau aplicație pentru muzicieni și să învăț Swift.", "Sorin", LocalDateTime.of(2025, 6, 20, 11, 55), "Fotografie", "Swift", "","img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de engleză pentru întreținere piscine", "Actor cu 10 ani experiență. Piscina acasă necesită întreținere profesională.", "Hamlet", LocalDateTime.of(2025, 6, 19, 14, 30), "Limba engleză", "Întreținere piscine", "","img/skill/skill-english.png"),

                new Announce(null, "Ofer cursuri de programare pentru wakeboard", "Programator antropolog cultural. Vreau wakeboard pentru activități acvatice de vară.", "Anca", LocalDateTime.of(2025, 6, 18, 18, 20), "Programare", "Wakeboard", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de gătit pentru jonglerie", "Gătesc ca analist financiar. Vreau jonglerie pentru îmbunătățirea concentrării.", "Razvan", LocalDateTime.of(2025, 6, 17, 9, 25), "Gătit", "Jonglerie", "","img/skill/skill-cooking.png"),

                new Announce(null, "Ofer cursuri de chitară pentru design logo", "Chitarist într-o trupă rock. Vreau să creez logo-uri pentru proiecte.", "Bass", LocalDateTime.of(2025, 6, 16, 15, 45), "Chitară", "Design logo", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de dans pentru snowboarding", "Instructor de dans cu clinică privată. Vreau snowboarding pentru iarnă.", "Delia", LocalDateTime.of(2025, 6, 15, 12, 10), "Dans", "Snowboarding", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de fotografie pentru reparații e-bike", "Fotograf în Brazilia de 4 ani. Bicicleta electrică are probleme tehnice.", "Pedro", LocalDateTime.of(2025, 6, 14, 16, 55), "Fotografie", "Reparații e-bike","", "img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de programare pentru escaladă", "Programator cu 7 ani experiență. Vreau escaladă pe pereți artificiali.", "Radu", LocalDateTime.of(2025, 6, 13, 13, 40), "Programare", "Escaladă", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de public speaking pentru branding", "Public speaking în muzica populară. Vreau identitate de brand pentru concerte.", "Folk", LocalDateTime.of(2025, 6, 12, 17, 25), "Public speaking", "Branding","", "img/skill/skill-public-speaking.png"),

                new Announce(null, "Ofer cursuri de engleză pentru surfing", "Profesor de engleză la muzeu. Vreau surfing pentru vacanțele exotice.", "Dino", LocalDateTime.of(2025, 6, 11, 10, 15), "Limba engleză", "Surfing", "","img/skill/skill-english.png"),

                new Announce(null, "Ofer cursuri de dans pentru programare Python", "Instructor de dans terapeut certificat. Vreau să automatizez procese din cabinet.", "Danca", LocalDateTime.of(2025, 6, 10, 14, 50), "Dans", "Python", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de chitară pentru bouldering", "Chitarist student în Coreea de Sud. Vreau bouldering pentru activitate.", "Kim", LocalDateTime.of(2025, 6, 9, 16, 35), "Chitară", "Bouldering", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de gătit pentru sisteme solare", "Gătesc în orchestra filarmonică. Panouri solare acasă necesită întreținere.", "Solar", LocalDateTime.of(2025, 6, 8, 11, 20), "Gătit", "Sisteme solare", "","img/skill/skill-cooking.png"),

                new Announce(null, "Ofer cursuri de fotografie pentru tai-chi", "Fotograf criminolog la universitate. Vreau tai-chi pentru echilibru mental.", "Detective", LocalDateTime.of(2025, 6, 7, 15, 5), "Fotografie", "Tai-chi", "","img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de programare pentru longboard", "Programator vorbesc Hindi nativ. Vreau longboarding pentru plimbări urbane.", "Raj", LocalDateTime.of(2025, 6, 6, 18, 30), "Programare", "Longboard", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de public speaking pentru parkour", "Public speaking stomatolog cu clinică. Vreau parkour pentru îmbunătățirea agilității.", "Smile", LocalDateTime.of(2025, 6, 5, 12, 45), "Public speaking", "Parkour","", "img/skill/skill-public-speaking.png"),

                new Announce(null, "Ofer cursuri de engleză pentru reparații drone", "Profesor de engleză în orchestră. Drone pentru fotografie aeriană cu probleme.", "Fagot", LocalDateTime.of(2025, 6, 4, 9, 10), "Limba engleză", "Reparații drone", "","img/skill/skill-english.png"),

                new Announce(null, "Ofer cursuri de chitară pentru slacklining", "Chitarist oceanograf la institut. Vreau slacklining pentru echilibru și concentrare.", "Ocean", LocalDateTime.of(2025, 6, 3, 14, 25), "Chitară", "Slacklining", "","img/skill/skill-guitar.png"),

                new Announce(null, "Ofer cursuri de gătit pentru didgeridoo", "Gătesc specialist HR cu experiență. Îmi place sunetul didgeridoo-ului aboriginal.", "Recursos", LocalDateTime.of(2025, 6, 2, 16, 0), "Gătit", "Didgeridoo", "","img/skill/skill-cooking.png"),

                new Announce(null, "Ofer cursuri de dans pentru rollerblading", "Instructor de dans optometrist cu clinică. Vreau rollerblading pentru weekend.", "Vision", LocalDateTime.of(2025, 6, 1, 13, 20), "Dans", "Rollerblading", "","img/skill/skill-dance.png"),

                new Announce(null, "Ofer cursuri de fotografie pentru programare Go", "Fotograf în orchestra militară. Vreau Go pentru proiecte de automatizare.", "Tuba", LocalDateTime.of(2025, 5, 31, 17, 45), "Fotografie", "Programare Go", "","img/skill/skill-photography.png"),

                new Announce(null, "Ofer cursuri de programare pentru kendo", "Programator meteorolog la serviciul național. Vreau kendo pentru disciplină mentală.", "Weather", LocalDateTime.of(2025, 5, 30, 10, 30), "Programare", "Kendo", "","img/skill/skill-programming.png"),

                new Announce(null, "Ofer cursuri de public speaking pentru free running", "Public speaking muzico-terapeut certificat. Vreau free running pentru condiție fizică.", "Harmony", LocalDateTime.of(2025, 5, 29, 15, 15), "Public speaking", "Free running", "","img/skill/skill-public-speaking.png"),

                new Announce(null, "Ofer cursuri de engleză pentru crossfit", "Profesor de engleză cu companii finlandeze. Vreau crossfit pentru condiție fizică.", "Suomi", LocalDateTime.of(2025, 5, 20, 12, 50), "Limba engleză", "Crossfit","", "img/skill/skill-english.png"));

                announceRepository.saveAll(announceList);

    }

    private void addCategories() throws Exception {
        List<Category> categoryList = List.of(
                new Category(1L, "Programare"),
                new Category(2L, "Design"),
                new Category(3L, "Fotografie"),
                new Category(4L, "Scriere"),
                new Category(5L, "Marketing"),
                new Category(6L, "Limbi străine"),
                new Category(7L, "Business"),
                new Category(8L, " Coaching"),
                new Category(9L, "DIY"),
                new Category(10L, "Artă"),
                new Category(11L, "Muzică")
        );
        categoryRepository.saveAll(categoryList);
    }


}
