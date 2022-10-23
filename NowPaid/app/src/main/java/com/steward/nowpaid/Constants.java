package com.steward.nowpaid;

public class Constants {
    public static final String[] categories = {"Todos", "Restaurantes", "Comedores", "Distribuidora", "Calzado", "Tienda", "Electronicos",  "Veterinaria"};
    public static final String[] categories_fix = {"Restaurantes", "Comedores", "Distribuidora", "Calzado", "Tienda", "Electronicos", "Veterinaria"};

    public static final String[] locations = {
            "Juigalpa, Chontales",
            "Acoyapa, Chontales",
            "La libertad, Chontales",
            "Santo Tomas, Chontales",
            "Camoapa, Boaco",
            "Boaco, Boaco",
            "Teustepe, Boaco",
            "Managua, Managua",
            "Tipitapa, Managua",
            "Masatepe, Masaya",
            "Masaya, Masaya",
            "Nindiri, Masaya",
            "Jinotepe, Carazo",
            "Diriomo, Carazo",
            "Somoto, Nueva Segovia",
            "San Carlos, Rio San Juan"
    };

    public static int getIndexLoc(String text){
        for(int i =0;i < locations.length; i++) {
            if(locations[i].startsWith(text)) {
                return i;
            }
        }
        return -1;
    }

    public static int getIndexCat(String text){
        for(int i =0;i < categories_fix.length; i++) {
            if(categories_fix[i].startsWith(text)) {
                return i + 1;
            }
        }
        return -1;
    }
}
