# YABaCaGa
## Lancement via Eclipse
Premièrement, une vidéo du fonctionnement de notre jeu a été rendue en même temps que le projet. Cette vidéo permet de voir le programme fonctionner même si vous avez des problèmes pour compiler ou exécuter le jeu à partir du code source.
Dans cette partie, nous allons expliquer comment compiler le projet pour pouvoir l’exécuter. 
* Installer une version récente d’Eclipse JEE. Notre version est : 2024-12 (4.34.0).
* Cloner le projet git dans un dossier (https://github.com/SylvanCourtiol/YABaCaGa). Ce dossier est le workspace eclipse.
* Ouvrir le workspace avec Eclipse. Si le projet YABaCaGa n’est pas présent dans la liste des projets, importer le projet (import > Existing projects into workspace).
* Télécharger le SDK de javaFX sur https://gluonhq.com/products/javafx/
* Extraire le SDK sur votre ordinateur.
* Dans éclipse, installer le module e(fx)clipse 3.8.0 (taper javafx dans la recherche du marketplace d’Eclipse).
* Une fois l'installation terminée du module, redémarrer éclipse comme proposé.
* Dans Window > preferences, chercher “user libraries”
* Créer une nouvelle user libraries avec le nom “JavaFX” et ajouter des external jars qui sont les jars présents dans le dossier lib des fichiers du SDK de javaFX.
* A cette étape, normalement, le code peut compiler. Si ce n’est pas le cas, vérifier que le build path du projet contiennent bien la user library JavaFX et les jars présents dans le dossier lib du projet.
* Pour exécuter le code, il faut run server.Server pour lancer le serveur. Pour les deux joueurs, il faut créer des run configuration qui run yabacaga.hmi.ClientMain. Cependant, pour les deux joueurs il faut ajouter les arguments de la vm suivant : “--module-path "ADD YOUR PATH/javafx-sdk-23.0.2/lib" --add-modules javafx.controls,javafx.graphics,javafx.fxml”. Il y a aussi des arguments du programme différent pour chaque joueur : par exemple “Pierre” pour l’un et “Michel” pour l’autre. Ces arguments sont importants car ils donnent le nom à l’instance de l’agent visible dans circle. Il n’est pas nécessaire que vos noms soient exactement Pierre et Michel mais il faut des noms différents pour chaque joueur.
* Pour exécuter le jeu, il faut lancer Circle avec le fichier “YABaCaGa.igssystem” présent à la racine du git avec Circle. Il faut activer le logiciel avec l’interface loopback et le port 40500. Ensuite, il faut activer le proxy dans Circle avec le port 8080. Il reste plus qu’à lancer le server.Server et les deux joueurs à partir des deux run configurations créées plus tôt.

Remarque : les interfaces ne sont pas responsible car c’est très compliqué en javaFX. Si les interfaces semblent comprimées, il faut vérifier la mise à l’échelle de votre écran. En effet, l’interface devrait passer sur un écran full HD (1920*1080 pixels) mais si le système agrandit vos interfaces cela peut casser l’interface.
