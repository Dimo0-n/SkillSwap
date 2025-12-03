# SkillSwap – Documentație Tehnică  
**Standard:** ISO/IEC/IEEE 26515 · Documentație Software  
**Versiune:** 1.0.0  
**Data:** 2025-12-04
**Clasificare:** Aplicație Web Enterprise  
**Autor:** Dumitru Frimu  

---

## 1. Identificare Sistem

### 1.1 Tipul aplicației
SkillSwap este o aplicație web enterprise dezvoltată în Java, destinată schimbului de competențe între utilizatori, pe baza de anunțuri și potrivire (matching).

### 1.2 Platformă și Framework-uri
- Java 17+
- Spring Boot 3.x
- Spring MVC · Spring Security · Spring Data JPA
- Thymeleaf + thymeleaf-extras-springsecurity

### 1.3 Arhitectură
- Model–View–Controller (MVC) pentru UI  
- REST API parțial  
- Arhitectură pe layere: **Controller → Service → Repository**  
- Persistență prin Hibernate (JPA)  
- RBAC (Role-Based Access Control)  
- Template Engine: Thymeleaf  

---

## 2. Cerințe Sistem

### 2.1 Cerințe Hardware
- CPU: minim 2 core  
- RAM: minim 2GB  
- Spațiu pe disc: 200MB  
- Suport Linux / Windows / macOS  

### 2.2 Cerințe Software
- Java 17+  
- Maven 3.8+  
- Spring Boot CLI (opțional)  
- MySQL / PostgreSQL / H2  
- Browser modern (Chrome, Firefox)

### 2.3 Dependențe externe
- Jitsi pentru meeting-uri video  
- SMTP server pentru email  
- Baza de date SQL  

---

## 3. Prezentare Generală a Sistemului

### 3.1 Descriere scurtă
SkillSwap permite utilizatorilor să publice anunțuri, să caute skill-uri, să contacteze alți utilizatori și să facă schimburi bazate pe competențe.

### 3.2 Ce ar trebui să facă dacă ar fi complet
- Profiluri detaliate și recomandări personalizate  
- Matching automat skill-uri ↔ nevoie  
- Mesagerie realtime + notificări  
- Video call integrat  
- Sistem reputație (rating)  
- Gestionare schimburi cu istorice  
- Administrare completă pentru admin  
- Conformitate GDPR și securitate extinsă  

---

## 4. Arhitectură Sistem

### 4.1 Diagrame UML *(de completat ulterior)*
Aici vor fi adăugate:
- Class Diagram  
- Use Case Diagram  
- Activity Diagram  
- Deployment Diagram  

### 4.2 Model de Date
Entități principale:
- **User**  
- **Role**  
- **Announce**  
- **Category**  
- **Contact**  

### 4.3 Fluxuri de date
- Utilizator → Autentificare → `AuthenticationService`  
- Utilizator → Publicare anunț → `AnnounceController` → `AnnounceService`  
- Utilizator → Contact → `ContactService` → Persistență  

---

## 5. Funcționalități principale

### 5.1 Autentificare & Înregistrare
- GET `/login`, `/register`  
- POST `/register/save` (creare user + parola criptată, rol `ROLE_USER`)  
- Logout → invalidare sesiune + ștergere `JSESSIONID`  

### 5.2 Roluri și Autorizare
- Roluri: `ROLE_ADMIN`, `ROLE_USER`  
- Protecție rute via Spring Security  
- Admin page: `/admin` (doar pentru admin)  

### 5.3 Anunțuri
- `/index` afișează ultimele 5 anunțuri  
- `/announces-list` → lista completă  
- Date demo încărcate la startup  

### 5.4 Categorii
- Populate automat la pornire  
- Folosite în UI  

### 5.5 Contact
- GET `/contact`  
- POST `/contact/faq` – salvare mesaj  

### 5.6 Chat (UI)
- UI pregătit, backend realtime neimplementat  

### 5.7 Profil utilizator
- `/profil` – doar autentificat  

### 5.8 Meeting/Jitsi
- Încarcă `jitsi-meet.html`  
- Backend integrabil ulterior  

---

## 6. Arhitectură Backend

### 6.1 Controllere
- `HomeController`  
- `AuthController`  
- `AnnounceController`  
- `ContactController`  
- `AdminController`  

### 6.2 Servicii
- `AuthService`  
- `AnnounceService`  
- `ContactService`  

### 6.3 Persistență (JPA)
Entități:
- User, Role, Announce, Category, Contact  

Repozitoare:
- `UserRepository`, `RoleRepository`, `AnnounceRepository`, `CategoryRepository`, `ContactRepository`  

### 6.4 Securitate
- `SecurityConfig`  
- `CustomUserDetailsService`  
- Parole criptate cu `BCryptPasswordEncoder`  

### 6.5 Inițializare date
- `RoleDataLoader`  
- `DataInitializer`  

### 6.6 Configurări suplimentare
- `HiddenHttpMethodFilter` pentru PUT/DELETE  
- Handlere globale pentru excepții  

---

## 7. Frontend (Thymeleaf + Active UI)
- Template-uri în `/templates`  
- Fragmente în `/templates/fragments`  
- CSS/JS în `/static`  
- Meniu condiționat de roluri  

---

## 8. Rute cheie

### Public
`/index`, `/announces-list`, `/categories-list`, `/contact`, `/chat`, `/post-details`, `/typography`, `/meeting`, `/404`

### Autentificare
`/login`, `/register`, `/register/save`, `/logout`

### Roluri
Buton Admin → `/admin` *(doar `ROLE_ADMIN`)*

---

## 9. Cum rulezi proiectul

1. Configurezi baza de date în `application.properties`  
2. Rulezi:
   - Linux/macOS: `./mvnw spring-boot:run`
   - Windows: `mvnw.cmd spring-boot:run`
3. Accesezi aplicația:  
   `http://localhost:8080/index`

---

## 10. Tehnologii
- Java 17+  
- Spring Boot  
- Spring MVC  
- Spring Security  
- Spring Data JPA  
- Thymeleaf  

---

## 11. Structura directoare

src/main/java/com/example/skillswap → backend (controllers, services, config)
src/resources/templates → template-uri HTML
src/resources/static → CSS, JS, imagini
src/resources/application.properties → configurări aplicație


---

## 12. Notă
UI-ul pentru Chat, Profil și Meeting este implementat, dar backend-ul poate fi extins pentru:
- WebSockets  
- Notificări realtime  
- Integrare Jitsi completă  
- Funcționalități avansate de messaging  

