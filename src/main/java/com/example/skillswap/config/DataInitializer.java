//package com.example.skillswap.config;
//
//import com.application.market.entity.Category;
//import com.application.market.entity.Product;
//import com.application.market.entity.PromoCode;
//import com.application.market.repository.CategoryRepository;
//import com.application.market.repository.PromoCodeRepository;
//import com.application.market.repository.ShopRepository;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Component;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.util.List;
//
//@Component
//public class DataInitializer {
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    @Autowired
//    private ShopRepository shopRepository;
//
//    @Autowired
//    private PromoCodeRepository promoCodeRepository;
//
//    public DataInitializer(CategoryRepository categoryRepository,
//                           ShopRepository shopRepository,
//                           PromoCodeRepository promoCodeRepository) {
//        this.categoryRepository = categoryRepository;
//        this.shopRepository = shopRepository;
//        this.promoCodeRepository = promoCodeRepository;
//    }
//
//
//    //pentru categorii
//    private byte[] loadImage(String imagePath, int width, int height) {
//        try {
//            // Încarcă imaginea din calea specificată
//            ClassPathResource resource = new ClassPathResource(imagePath);
//            InputStream inputStream = resource.getInputStream();
//
//            // Citește imaginea ca BufferedImage
//            BufferedImage originalImage = ImageIO.read(inputStream);
//
//            // Redimensionează imaginea la dimensiunea dorită
//            BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
//            Graphics2D g = resizedImage.createGraphics();
//            g.drawImage(originalImage, 0, 0, width, height, null);
//            g.dispose();
//
//            // Convertește imaginea redimensionată într-un array de bytes
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ImageIO.write(resizedImage, "jpg", baos);
//            return baos.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    @PostConstruct
//    public void init() throws Exception {
//        addCategories();
//        addProducts();
//        addPromoCodes();
//    }
//
//    private void addCategories() {
//
//        List<Category> categoriesList = List.of(
//                new Category(1, "Fruits and vegetables",
//                        loadImage("static/images/categories/category1.jpg", 161, 160)),
//                new Category(2, "Dairy and Eggs",
//                        loadImage("static/images/categories/category2.jpg", 161, 160)),
//                new Category(3, "Meat and Poultry",
//                        loadImage("static/images/categories/category3.jpg", 161, 160)),
//                new Category(4, "Seafood",
//                        loadImage("static/images/categories/category4.jpg", 161, 160)),
//                new Category(5, "Bakery and Bread",
//                        loadImage("static/images/categories/category5.jpg", 161, 160)),
//                new Category(6, "Canned Goods",
//                        loadImage("static/images/categories/category6.jpg", 161, 160)),
//                new Category(7, "Frozen Foods",
//                        loadImage("static/images/categories/category7.jpg", 161, 160)),
//                new Category(8, "Pasta and Rice",
//                        loadImage("static/images/categories/category8.jpg", 161, 160)),
//                new Category(9, "Breakfast Foods",
//                        loadImage("static/images/categories/category9.jpg", 161, 160)),
//                new Category(10, "Snacks and Chips",
//                        loadImage("static/images/categories/category10.jpg", 161, 160)),
//                new Category(11, "Beverages",
//                        loadImage("static/images/categories/category11.jpg", 161, 160)),
//                new Category(12, "Spices and Seasonings",
//                        loadImage("static/images/categories/category12.jpg", 161, 160)),
//                new Category(13, "Baby Food and Formula",
//                        loadImage("static/images/categories/category13.jpg", 161, 160)),
//                new Category(14, "Health and Wellness",
//                        loadImage("static/images/categories/category14.jpg", 161, 160)),
//                new Category(15, "Household Supplies",
//                        loadImage("static/images/categories/category15.jpg", 161, 160)),
//                new Category(16, "Personal Care",
//                        loadImage("static/images/categories/category16.jpg", 161, 160)),
//                new Category(17, "Pet Food and Supplies",
//                        loadImage("static/images/categories/category17.jpg", 161, 160))
//        );
//
//        categoryRepository.saveAll(categoriesList);
//
//    }
//
//    private void addPromoCodes() throws Exception {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//        List<PromoCode> promoCodesList = List.of(
//                new PromoCode(null, "WELCOME10", 10, sdf.parse("2025-12-31")),
//                new PromoCode(null, "SUMMER15", 15, sdf.parse("2025-08-31")),
//                new PromoCode(null, "BLACKFRIDAY20", 20, sdf.parse("2025-11-30")),
//                new PromoCode(null, "NEWYEAR25", 25, sdf.parse("2026-01-15"))
//        );
//
//        promoCodeRepository.saveAll(promoCodesList);
//    }
//
//    //pentru producte
//    private byte[] loadImage(String imagePath) {
//        try {
//            // Dimensiuni fixe pentru imagine
//            int width = 200;
//            int height = 210;
//
//            // Încarcă imaginea din calea specificată
//            ClassPathResource resource = new ClassPathResource(imagePath);
//            InputStream inputStream = resource.getInputStream();
//
//            // Citește imaginea ca BufferedImage
//            BufferedImage originalImage = ImageIO.read(inputStream);
//
//            // Redimensionează imaginea la dimensiunea dorită
//            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//            Graphics2D g = resizedImage.createGraphics();
//            g.drawImage(originalImage, 0, 0, width, height, null);
//            g.dispose();
//
//            // Convertește imaginea redimensionată într-un array de bytes
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ImageIO.write(resizedImage, "jpg", baos);
//            return baos.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//
//    private void addProducts() {
//        List<Product> productList = List.of(
//                // 1 catergorie de produse-----------------------------------------------------------------------
//
//                new Product(1L, categoryRepository.findById(1).get(),
//                        "Apple",
//                        "Un produs gustos și proaspăt, inspirat de mere naturale.",
//                        13.99, 10, 4.5, 120,
//                        loadImage("static/images/products/1/apple.jpg")),
//
//                new Product(2L, categoryRepository.findById(1).get(),
//                        "Banana",
//                        "Produs 100% natural, inspirat de dulceața bananelor proaspete.",
//                        14.49, 12, 4.5, 120,
//                        loadImage("static/images/products/1/banana.jpg")),
//
//                new Product(3L, categoryRepository.findById(1).get(),
//                        "Bell Peppers",
//                        "O alegere sănătoasă, inspirată de prospețimea ardeilor grași.",
//                        14.99, 15, 4.5, 120,
//                        loadImage("static/images/products/1/bellPeppers.jpg")),
//
//                new Product(4L, categoryRepository.findById(1).get(),
//                        "Blueberry",
//                        "Un produs natural și răcoritor, inspirat de afinele proaspete.",
//                        13.49, 8, 4.5, 120,
//                        loadImage("static/images/products/1/blueberrie.jpg")),
//
//                new Product(5L, categoryRepository.findById(1).get(),
//                        "Broccoli",
//                        "Sănătate în fiecare picătură, inspirată de prospețimea broccoliului.",
//                        15.99, 10, 4.5, 120,
//                        loadImage("static/images/products/1/broccoli.jpg")),
//
//                new Product(6L, categoryRepository.findById(1).get(),
//                        "Carrot",
//                        "O băutură inspirată de morcovii proaspeți, plină de vitalitate.",
//                        14.29, 9, 4.5, 120,
//                        loadImage("static/images/products/1/carrot.jpg")),
//
//                new Product(7L, categoryRepository.findById(1).get(),
//                        "Cucumber",
//                        "Răcoritor și natural, inspirat de castraveții proaspeți.",
//                        15.49, 11, 4.5, 120,
//                        loadImage("static/images/products/1/cucumber.jpg")),
//
//                new Product(8L, categoryRepository.findById(1).get(),
//                        "Garlic",
//                        "Un produs unic, inspirat de natura autentică a usturoiului.",
//                        12.99, 7, 4.5, 120,
//                        loadImage("static/images/products/1/garlic.jpg")),
//
//                new Product(9L, categoryRepository.findById(1).get(),
//                        "Grape",
//                        "Un produs răcoritor, inspirat de dulceața naturală a strugurilor.",
//                        16.49, 15, 4.5, 120,
//                        loadImage("static/images/products/1/grape.jpg")),
//
//                new Product(10L, categoryRepository.findById(1).get(),
//                        "Mango",
//                        "Prospețime tropicală, inspirată de gustul autentic al mango-ului.",
//                        14.99, 13, 4.5, 120,
//                        loadImage("static/images/products/1/mangoe.jpg")),
//
//                new Product(11L, categoryRepository.findById(1).get(),
//                        "Onion",
//                        "Un produs natural, inspirat de aroma intensă a cepei.",
//                        13.99, 6, 4.5, 120,
//                        loadImage("static/images/products/1/onions.jpg")),
//
//                new Product(12L, categoryRepository.findById(1).get(),
//                        "Orange",
//                        "Revitalizant și gustos, inspirat de portocalele proaspete.",
//                        15.29, 14, 4.5, 120,
//                        loadImage("static/images/products/1/orange.jpg")),
//
//                new Product(13L, categoryRepository.findById(1).get(),
//                        "Peach",
//                        "Prospețime și savoare, inspirate de piersicile naturale.",
//                        15.79, 12, 4.5, 120,
//                        loadImage("static/images/products/1/peache.jpg")),
//
//                new Product(14L, categoryRepository.findById(1).get(),
//                        "Pineapple",
//                        "Un produs exotic, inspirat de savoarea ananasului proaspăt.",
//                        14.49, 10, 4.5, 120,
//                        loadImage("static/images/products/1/pineapples.jpg")),
//
//                new Product(15L, categoryRepository.findById(1).get(),
//                        "Potato",
//                        "Inspirat de natura simplă și autentică a cartofilor.",
//                        12.49, 5, 4.5, 120,
//                        loadImage("static/images/products/1/potatoe.jpg")),
//
//                new Product(16L, categoryRepository.findById(1).get(),
//                        "Spinach",
//                        "Sănătate pură, inspirată de prospețimea spanacului natural.",
//                        13.49, 6, 4.5, 120,
//                        loadImage("static/images/products/1/spinach.jpg")),
//
//                new Product(17L, categoryRepository.findById(1).get(),
//                        "Strawberry",
//                        "O alegere dulce și proaspătă, inspirată de căpșunile de sezon.",
//                        15.99, 13, 4.5, 120,
//                        loadImage("static/images/products/1/strawberrie.jpg")),
//
//                new Product(18L, categoryRepository.findById(1).get(),
//                        "Tomato",
//                        "Fresh and juicy tomato, perfect for salads or cooking.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/1/tomatoe.jpg")),
//
//                new Product(19L, categoryRepository.findById(1).get(),
//                        "Watermelon",
//                        "Sweet and refreshing watermelon, ideal for a summer snack.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/1/watermelon.jpg")),
//
//                new Product(20L, categoryRepository.findById(1).get(),
//                        "Zucchini",
//                        "Fresh and healthy zucchini, great for cooking or adding to salads.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/1/zucchini.jpg")),
//
//                //2 categorie de produse--------------------------------------------------------------------
//
//                new Product(21L, categoryRepository.findById(2).get(),
//                        "Butter",
//                        "Un produs gustos și proaspăt, inspirat de prospețimea untului natural.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/2/butter.jpg")),
//
//                new Product(22L, categoryRepository.findById(2).get(),
//                        "Cheese",
//                        "O alegere delicioasă, cu savoarea autentică a brânzei naturale.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/2/cheese.jpg")),
//
//                new Product(23L, categoryRepository.findById(2).get(),
//                        "Cottage Cheese",
//                        "Un produs proaspăt și sănătos, inspirat de gustul brânzei de vaci naturale.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/2/cottageCheese.jpg")),
//
//                new Product(24L, categoryRepository.findById(2).get(),
//                        "Eggs",
//                        "Prospețime naturală în fiecare ou, pentru un gust autentic.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/2/eggs.jpg")),
//
//                new Product(25L, categoryRepository.findById(2).get(),
//                        "Milk",
//                        "Laptele proaspăt și gustos, plin de nutrienți esențiali.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/2/milk.jpg")),
//
//                new Product(26L, categoryRepository.findById(2).get(),
//                        "Mozzarella",
//                        "O alegere cremoasă și naturală, inspirată de mozzarella proaspătă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/2/mozzarella.jpg")),
//
//                new Product(27L, categoryRepository.findById(2).get(),
//                        "Ricotta",
//                        "Prospețime și textură fină, inspirate de ricotta autentică.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/2/ricotta.jpg")),
//
//                new Product(28L, categoryRepository.findById(2).get(),
//                        "Sour Cream",
//                        "Un produs cremos și delicios, inspirat de smântâna naturală.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/2/sourCream.jpg")),
//
//                new Product(29L, categoryRepository.findById(2).get(),
//                        "Yogurt",
//                        "Un produs proaspăt și sănătos, inspirat de iaurtul natural.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/2/yogurt.jpg")),
//
//                //3 categorie -------------------------------------------------------------------------------
//
//                new Product(30L, categoryRepository.findById(3).get(),
//                        "Bacon",
//                        "Bacon delicios și crocant, perfect pentru un mic dejun sățios.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/3/bacon.jpg")),
//
//                new Product(31L, categoryRepository.findById(3).get(),
//                        "Beef Steak",
//                        "Fraged și suculent, biftec de vită de calitate superioară.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/3/beefSteak.jpg")),
//
//                new Product(32L, categoryRepository.findById(3).get(),
//                        "Chicken",
//                        "Carne de pui proaspătă și gustoasă, ideală pentru orice rețetă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/3/chicken.jpg")),
//
//                new Product(34L, categoryRepository.findById(3).get(),
//                        "Chicken Breast",
//                        "Piept de pui slab și gustos, perfect pentru mese sănătoase.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/3/chickenBreast.jpg")),
//
//                new Product(35L, categoryRepository.findById(3).get(),
//                        "Chicken Thighs",
//                        "Pulpe de pui fragede și delicioase, ideale pentru grătar.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/3/chickenThighs.png")),
//
//                new Product(36L, categoryRepository.findById(3).get(),
//                        "Chicken Wings",
//                        "Aripioare de pui crocante, perfecte pentru orice ocazie.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/3/chickenWings.jpg")),
//
//                new Product(37L, categoryRepository.findById(3).get(),
//                        "Ham",
//                        "Șuncă fină și savuroasă, perfectă pentru sandvișuri.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/3/ham.jpg")),
//
//                new Product(38L, categoryRepository.findById(3).get(),
//                        "Pork Chops",
//                        "Cotlete de porc suculente, ideale pentru grătar.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/3/porkChops.jpg")),
//
//                new Product(39L, categoryRepository.findById(3).get(),
//                        "Sausages",
//                        "Cârnați aromați, perfecți pentru un grătar delicios.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/3/sausages.jpg")),
//
//                //4 categorie -----------------------------------------------------------------------------------------
//
//                new Product(40L, categoryRepository.findById(4).get(),
//                        "Crab Legs",
//                        "Picioare de crab proaspete, delicate și bogate în arome de mare.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/4/crabLegs.jpg")),
//
//                new Product(41L, categoryRepository.findById(4).get(),
//                        "Lobster",
//                        "Homar de calitate superioară, perfect pentru mese sofisticate.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/4/lobster.jpg")),
//
//                new Product(42L, categoryRepository.findById(4).get(),
//                        "Oysters",
//                        "Stridii proaspete, ideale pentru un gust autentic al mării.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/4/oysters.jpg")),
//
//                new Product(43L, categoryRepository.findById(4).get(),
//                        "Salmon",
//                        "Somon fraged și delicios, perfect pentru preparate rafinate.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/4/salmon.jpg")),
//
//                new Product(44L, categoryRepository.findById(4).get(),
//                        "Shrimp",
//                        "Creveți savuroși și proaspeți, ideali pentru diverse preparate.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/4/shrimp.jpg")),
//
//                new Product(45L, categoryRepository.findById(4).get(),
//                        "Tuna Steaks",
//                        "Fileuri de ton de înaltă calitate, perfecte pentru preparate speciale.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/4/tunaSteaks.jpg")),
//
//                //5 categorie -----------------------------------------------------------------------------------
//
//                new Product(46L, categoryRepository.findById(5).get(),
//                        "Bagels",
//                        "Bagels pufoși, ideali pentru un mic dejun delicios.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/5/bagels.jpg")),
//
//                new Product(47L, categoryRepository.findById(5).get(),
//                        "Baguette",
//                        "Baguette tradițională, crocantă la exterior și moale în interior.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/5/baguette.jpg")),
//
//                new Product(48L, categoryRepository.findById(5).get(),
//                        "Ciabatta",
//                        "Ciabatta italiană autentică, perfectă pentru sandvișuri savuroase.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/5/ciabatta.jpg")),
//
//                new Product(49L, categoryRepository.findById(5).get(),
//                        "Croissants",
//                        "Croissante proaspete, delicate și pline de savoare.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/5/croissants.jpg")),
//
//                new Product(50L, categoryRepository.findById(5).get(),
//                        "Muffins",
//                        "Brioșe pufoase, ideale pentru un desert rapid și gustos.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/5/muffins.jpg")),
//
//                new Product(51L, categoryRepository.findById(5).get(),
//                        "White Bread",
//                        "Pâine albă moale, perfectă pentru sandvișuri clasice.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/5/whiteBread.jpg")),
//
//                new Product(52L, categoryRepository.findById(5).get(),
//                        "Whole Wheat Bread",
//                        "Pâine integrală sănătoasă, bogată în fibre și gust autentic.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/5/wholeWheatBread.jpg")),
//                //6 categorie ------------------------------------------------------------------------
//
//                new Product(53L, categoryRepository.findById(6).get(),
//                        "Beans",
//                        "Fasole proaspătă și sănătoasă, perfectă pentru diverse preparate.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/6/beans.jpg")),
//
//                new Product(54L, categoryRepository.findById(6).get(),
//                        "Corn",
//                        "Porumb proaspăt și dulce, ideal pentru o gustare rapidă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/6/corn.jpg")),
//
//                new Product(55L, categoryRepository.findById(6).get(),
//                        "Mushrooms",
//                        "Ciuperci proaspete, un ingredient versatil pentru orice masă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/6/mushrooms.jpg")),
//
//                new Product(56L, categoryRepository.findById(6).get(),
//                        "Peas",
//                        "Mazăre verde, proaspătă și delicioasă, perfectă pentru diverse preparate.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/6/peas.jpg")),
//
//                new Product(57L, categoryRepository.findById(6).get(),
//                        "Tomato",
//                        "Roșii coapte și zemoase, ideale pentru salate și preparate de vară.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/6/tomatoe.jpg")),
//
//                new Product(58L, categoryRepository.findById(6).get(),
//                        "Tuna",
//                        "Ton proaspăt, o sursă excelentă de proteine pentru mesele tale.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/6/tuna.jpg")),
//                // 7 categorie ------------------------------------------------------------------------------------
//
//                new Product(59L, categoryRepository.findById(7).get(),
//                        "Berries",
//                        "Fructe de pădure proaspete, pline de vitamine și antioxidanți.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/7/berrie.jpg")),
//
//                new Product(60L, categoryRepository.findById(7).get(),
//                        "French Fries",
//                        "Cartofi prăjiți crocanți și delicioși, perfecți ca garnitură.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/7/frenchFrie.jpg")),
//
//                new Product(61L, categoryRepository.findById(7).get(),
//                        "Ice Cream",
//                        "Înghețată cremoasă, disponibilă într-o varietate de arome.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/7/iceCrem.jpg")),
//
//                new Product(62L, categoryRepository.findById(7).get(),
//                        "Pizza",
//                        "Pizza gustoasă cu ingrediente proaspete, perfectă pentru orice ocazie.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/7/pizza.jpg")),
//
//                new Product(63L, categoryRepository.findById(7).get(),
//                        "Vegetables",
//                        "Legume proaspete și sănătoase, esențiale într-o alimentație echilibrată.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/7/vegetable.jpg")),
//                // 8 categorie ------------------------------------------------------------------------------------
//
//                new Product(64L, categoryRepository.findById(8).get(),
//                        "Fusilli",
//                        "Paste Fusilli, ideale pentru sosuri dense și gătit rapid.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/8/fusilli.jpg")),
//
//                new Product(65L, categoryRepository.findById(8).get(),
//                        "Linguine",
//                        "Paste Linguine subțiri și delicioase, perfecte pentru sosuri pe bază de fructe de mare.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/8/linguine.jpg")),
//
//                new Product(66L, categoryRepository.findById(8).get(),
//                        "Penne",
//                        "Paste Penne, ideale pentru sosuri groase sau preparate coapte.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/8/penne.jpg")),
//
//                new Product(67L, categoryRepository.findById(8).get(),
//                        "Red Rice",
//                        "Orez roșu, bogat în fibre și nutrienți, perfect pentru preparate sănătoase.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/8/redRice.jpg")),
//
//                new Product(68L, categoryRepository.findById(8).get(),
//                        "Rice",
//                        "Orez alb sau brun, esențial pentru o dietă echilibrată.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/8/rice.jpg")),
//
//                new Product(69L, categoryRepository.findById(8).get(),
//                        "Rigatoni",
//                        "Paste Rigatoni, perfecte pentru preparate cu sosuri bogate.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/8/rigatoni.jpg")),
//
//                new Product(70L, categoryRepository.findById(8).get(),
//                        "Spaghetti",
//                        "Spaghetti clasice, ideale pentru toate tipurile de sosuri.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/8/spaghetti.jpg")),
//
//                new Product(71L, categoryRepository.findById(8).get(),
//                        "Sushi Rice",
//                        "Orez pentru sushi, cu o textură lipicioasă perfectă pentru preparate japoneze.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/8/sushiRice.jpg")),
//                // 9 categorie ---------------------------------------------------------------------------------
//
//                new Product(72L, categoryRepository.findById(9).get(),
//                        "Granola",
//                        "Granola delicioasă și crocantă, perfectă pentru mic dejun sau gustări sănătoase.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/9/granola.jpg")),
//
//                new Product(73L, categoryRepository.findById(9).get(),
//                        "Oatmeal",
//                        "Fulgi de ovăz, o alegere sănătoasă și hrănitoare pentru micul dejun.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/9/oatmeal.jpg")),
//
//                new Product(74L, categoryRepository.findById(9).get(),
//                        "Pancakes",
//                        "Clătite pufoase, delicioase și rapide de preparat pentru un mic dejun perfect.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/9/pancakes.jpg")),
//
//                new Product(75L, categoryRepository.findById(9).get(),
//                        "Smoothie Mixes",
//                        "Amestecuri de smoothie-uri sănătoase, pline de vitamine și gust delicios.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/9/smoothieMixes.jpg")),
//
//                new Product(76L, categoryRepository.findById(9).get(),
//                        "Waffle",
//                        "Vafe crocante și delicioase, ideale pentru un mic dejun rapid sau desert.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/9/waffle.jpg")),
//                //10 categorie -----------------------------------------------------------------------------------
//
//                new Product(77L, categoryRepository.findById(10).get(),
//                        "Chips",
//                        "Cartofi prăjiți crocanți, o gustare perfectă pentru orice moment al zilei.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/10/chips.jpg")),
//
//                new Product(78L, categoryRepository.findById(10).get(),
//                        "Granola Bars",
//                        "Batoane de granola sănătoase, ideale pentru o gustare energizantă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/10/granolaBars.jpg")),
//
//                new Product(79L, categoryRepository.findById(10).get(),
//                        "Popcorn",
//                        "Popcorn proaspăt, o gustare delicioasă și ușoară pentru filme sau pauze.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/10/popcorn.jpg")),
//
//                new Product(80L, categoryRepository.findById(10).get(),
//                        "Pretzels",
//                        "Pretzels sărate și crocante, gustarea perfectă pentru orice ocazie.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/10/pretzels.jpg")),
//
//                new Product(81L, categoryRepository.findById(10).get(),
//                        "Tortilla",
//                        "Tortilla gustoasă, perfectă pentru tacos, burritos sau gustări rapide.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/10/tortilla.jpg")),
//                // 11 categorie --------------------------------------------------------------------------------
//
//                new Product(82L, categoryRepository.findById(11).get(),
//                        "Coffee",
//                        "Cafea proaspăt preparată, ideală pentru o porție de energie la începutul zilei.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/11/coffee.jpg")),
//
//                new Product(83L, categoryRepository.findById(11).get(),
//                        "Cola Energy",
//                        "Băutură energizantă cu gust de cola, perfectă pentru momente de viteză.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/11/colaEnergy.jpg")),
//
//                new Product(84L, categoryRepository.findById(11).get(),
//                        "Ice Tea",
//                        "Ceai rece revigorant, ideal pentru zilele călduroase sau pentru o pauză răcoritoare.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/11/iceTea.jpg")),
//
//                new Product(85L, categoryRepository.findById(11).get(),
//                        "Juice",
//                        "Suc natural, proaspăt, plin de vitamine și gust.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/11/juice.jpg")),
//
//                new Product(86L, categoryRepository.findById(11).get(),
//                        "Monster",
//                        "Băutură energizantă cu un gust puternic, ideală pentru a te menține activ pe parcursul zilei.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/11/monster.jpg")),
//
//                new Product(87L, categoryRepository.findById(11).get(),
//                        "Redbull",
//                        "Băutură energizantă Redbull, un clasic pentru momentele care cer energie.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/11/redbull.jpg")),
//
//                new Product(88L, categoryRepository.findById(11).get(),
//                        "Water",
//                        "Apă pură, esențială pentru hidratare și bunăstare.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/11/water.jpg")),
//                //12 categorie -----------------------------------------------------------------------------------
//
//                new Product(89L, categoryRepository.findById(12).get(),
//                        "Basil",
//                        "Basilicum proaspăt, ideal pentru adăugat în preparate culinare pentru un gust aromatic.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/12/basil.jpg")),
//
//                new Product(90L, categoryRepository.findById(12).get(),
//                        "Chilli",
//                        "Ardei iute, perfect pentru preparatele picante și pentru iubitorii de gusturi intense.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/12/chilli.jpg")),
//
//                new Product(91L, categoryRepository.findById(12).get(),
//                        "Cinnamon",
//                        "Scorțișoară măcinată, folosită în deserturi și băuturi, pentru un gust dulce și picant.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/12/cinnamon.jpg")),
//
//                new Product(92L, categoryRepository.findById(12).get(),
//                        "Cumin",
//                        "Chimen, un condiment cu un gust distinctiv, folosit în preparate din Orientul Mijlociu și India.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/12/cumin.jpg")),
//
//                new Product(93L, categoryRepository.findById(12).get(),
//                        "Garlic",
//                        "Usturoi proaspăt, indispensabil în bucătăria mediteraneană pentru un gust intens și sănătos.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/12/garlic.jpg")),
//
//                new Product(94L, categoryRepository.findById(12).get(),
//                        "Oregano",
//                        "Oregano, condiment aromatic pentru preparate din pizza, paste sau carne.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/12/oregano.jpg")),
//
//                new Product(95L, categoryRepository.findById(12).get(),
//                        "Paprika",
//                        "Paprika, un condiment dulce sau picant folosit pentru a adăuga culoare și gust preparatelor.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/12/paprika.jpg")),
//
//                new Product(96L, categoryRepository.findById(12).get(),
//                        "Salt",
//                        "Sare de mare, esențială în orice bucătărie pentru a condimenta preparatele.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/12/salt.jpg")),
//
//                new Product(97L, categoryRepository.findById(12).get(),
//                        "Turmeric",
//                        "Curcuma, un condiment cu proprietăți antiinflamatorii și un gust distinctiv, folosit în curry și alte preparate.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/12/turmeric.jpg")),
//                // 13 categorie ---------------------------------------------------------------------------------
//
//                new Product(98L, categoryRepository.findById(13).get(),
//                        "Baby Food",
//                        "Aliment pentru bebeluși, bogat în nutrienți esențiali pentru dezvoltarea sănătoasă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/13/babyFood.jpg")),
//
//                new Product(99L, categoryRepository.findById(13).get(),
//                        "Baby Rice Cereal",
//                        "Fulgi de orez pentru bebeluși, ușor de digerat și perfect pentru începuturile alimentației solide.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/13/babyRiceCereal.jpg")),
//
//                new Product(100L, categoryRepository.findById(13).get(),
//                        "Pureed Apples",
//                        "Piure de mere, o opțiune naturală și sănătoasă pentru bebeluși.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/13/pureedApples.jpg")),
//
//                new Product(101L, categoryRepository.findById(13).get(),
//                        "Pureed Carrots",
//                        "Piure de morcovi, o sursă excelentă de vitamine pentru bebeluși.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/13/pureedCarrots.jpg")),
//
//                new Product(102L, categoryRepository.findById(13).get(),
//                        "Pureed Peas",
//                        "Piure de mazăre, o variantă hrănitoare și gustoasă pentru bebeluși.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/13/pureedPeas.jpg")),
//
//                new Product(103L, categoryRepository.findById(13).get(),
//                        "Toddler Drink",
//                        "Băutură specială pentru copii, ușor de băut și plină de vitamine esențiale pentru dezvoltarea lor.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/13/toddlerDrink.jpg")),
//                //14 categorie ---------------------------------------------------------------------------------------
//
//                new Product(104L, categoryRepository.findById(14).get(),
//                        "Herbal Tea",
//                        "Ceai din plante, calmant și revitalizant, perfect pentru copii.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/14/herbalTea.jpg")),
//
//                new Product(105L, categoryRepository.findById(14).get(),
//                        "Multivitamins",
//                        "Multivitamine pentru copii, esențiale pentru creșterea și dezvoltarea lor sănătoasă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/14/multivitamins.jpg")),
//
//                new Product(106L, categoryRepository.findById(14).get(),
//                        "Nutritional Bars",
//                        "Batoane nutriționale, ideale pentru gustări sănătoase și energizante pentru copii.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/14/nutritionalBars.jpg")),
//
//                new Product(107L, categoryRepository.findById(14).get(),
//                        "Omega-3",
//                        "Omega-3 pentru copii, benefic pentru dezvoltarea creierului și a ochilor.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/14/omega3.jpg")),
//
//                new Product(108L, categoryRepository.findById(14).get(),
//                        "Probiotic",
//                        "Probiotice pentru copii, susțin un sistem digestiv sănătos și imunitate puternică.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/14/probiotic.jpg")),
//
//                new Product(109L, categoryRepository.findById(14).get(),
//                        "Protein",
//                        "Proteine pentru copii, esențiale pentru dezvoltarea musculară și creșterea sănătoasă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/14/protein.jpg")),
//                //15 categorie ----------------------------------------------------------------------------------
//
//                new Product(110L, categoryRepository.findById(15).get(),
//                        "Cleaner",
//                        "Detergent de curățare, eficient pentru igienizarea diverselor suprafețe.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/15/cleaner.jpg")),
//
//                new Product(111L, categoryRepository.findById(15).get(),
//                        "Dish Soap",
//                        "Săpun de vase, puternic în îndepărtarea grăsimii și delicat cu mâinile.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/15/dishSoap.jpg")),
//
//                new Product(112L, categoryRepository.findById(15).get(),
//                        "Glass Cleaner",
//                        "Detergent pentru curățarea sticlei și oglinzilor, lăsându-le strălucitoare.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/15/glassCleaner.jpg")),
//
//                new Product(113L, categoryRepository.findById(15).get(),
//                        "Paper Towels",
//                        "Șervețele de hârtie, absorbante și eficiente pentru curățarea rapidă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/15/paperTowels.jpg")),
//
//                new Product(114L, categoryRepository.findById(15).get(),
//                        "Sponges",
//                        "Bureți de curățat, ideali pentru îndepărtarea murdăriei de pe suprafețele delicate.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/15/sponges.jpg")),
//
//                new Product(115L, categoryRepository.findById(15).get(),
//                        "Toilet Paper",
//                        "Hârtie igienică, moale și rezistentă, pentru confortul zilnic.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/15/toiletPaper.jpg")),
//
//                new Product(116L, categoryRepository.findById(15).get(),
//                        "Trash Bags",
//                        "Sacii de gunoi, rezistenți și ideali pentru colectarea deșeurilor.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/15/trashBags.jpg")),
//                //16 categorie ------------------------------------------------------------------------------
//
//                new Product(117L, categoryRepository.findById(16).get(),
//                        "Body Wash",
//                        "Gel de duș, delicat cu pielea, lăsând-o hidratată și proaspătă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/16/bodyWash.jpg")),
//
//                new Product(118L, categoryRepository.findById(16).get(),
//                        "Toothpaste",
//                        "Pasta de dinți, cu proprietăți antibacteriene și pentru albire.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/16/colgate.jpg")),
//
//                new Product(119L, categoryRepository.findById(16).get(),
//                        "Conditioner",
//                        "Balsam pentru păr, hrănitor și ușor de folosit pentru un păr mătăsos.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/16/conditioner.jpg")),
//
//                new Product(120L, categoryRepository.findById(16).get(),
//                        "Deodorant",
//                        "Deodorant cu protecție de lungă durată, pentru un miros proaspăt toată ziua.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/16/deodorant.jpg")),
//
//                new Product(121L, categoryRepository.findById(16).get(),
//                        "Facial Cleaner",
//                        "Curățător facial, delicat cu pielea, pentru o curățare profundă și revitalizare.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/16/facialCleaner.jpg")),
//
//                new Product(122L, categoryRepository.findById(16).get(),
//                        "Moisturizer",
//                        "Crema hidratantă, ideală pentru menținerea unui ten catifelat și hidratat.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/16/moisturizer.jpg")),
//
//                new Product(123L, categoryRepository.findById(16).get(),
//                        "Shampoo",
//                        "Șampon pentru păr, oferind curățare și îngrijire, lăsând părul strălucitor.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/16/shampoo.jpg")),
//
//                new Product(124L, categoryRepository.findById(16).get(),
//                        "Sunscreen",
//                        "Crema de protecție solară, cu factor de protecție ridicat, pentru protecție împotriva razelor UV.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/16/sunscreen.jpg")),
//                //17 categorie ---------------------------------------------------------------------------------------
//
//                new Product(125L, categoryRepository.findById(17).get(),
//                        "Bird Seed Mix",
//                        "Amestec de semințe pentru păsări, ideal pentru hrănirea păsărilor sălbatice și domestice.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/17/birdSeedMix.jpg")),
//
//                new Product(126L, categoryRepository.findById(17).get(),
//                        "Cat Food",
//                        "Mâncare pentru pisici, echilibrată și bogată în nutrienți esențiali pentru o sănătate optimă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/17/catFood.jpg")),
//
//                new Product(127L, categoryRepository.findById(17).get(),
//                        "Dog Food",
//                        "Mâncare pentru câini, cu un conținut de proteine de înaltă calitate, pentru o dezvoltare sănătoasă.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/17/dogFood.jpg")),
//
//                new Product(128L, categoryRepository.findById(17).get(),
//                        "Puppy Training Treats",
//                        "Biscuiți de antrenament pentru căței, ideali pentru recompensarea comportamentului pozitiv.",
//                        15.99, 20, 4.5, 120,
//                        loadImage("static/images/products/17/puppyTrainingTreats.jpg"))
//
//        );
//
//        shopRepository.saveAll(productList);
//
//    }
//
//}
