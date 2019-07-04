import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.NoSuchElementException;

public class initUI extends JFrame {

  String conveyLink = null;

  //Returns the convey link given a movie name
  private String getConvey(String[][] stringVec, String movieName) {

    for (int i = 0; i < stringVec.length; i++) {
      if (stringVec[i][0].contains(movieName)) {
        return stringVec[i][1];
      }
    }

    return null;
  }

  //Creates the query and gets all data
  private String buildQueryNYTIMESALL() {
    return
        "PREFIX topMovies: <http://www.semanticweb.org/manohara/ontologies/2016/9/untitled-ontology-10#> " +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT ?name ?IsCritiqueOf ?Conveys " +
            "WHERE {" +
            "      ?resource topMovies:IsCritiqueOf ?IsCritiqueOf . " +
            "      ?resource foaf:name ?name . " +
            "      ?resource topMovies:Conveys ?Conveys . " +
            "      }";
  }

  //Vector to get both name of the movie and convey's link
  private String[][] getNameAndConveyNYTIMESALL(Query query, Model model) {

    //Three ResultSet because you can't walk through without method next() which looses the start pointer
    QueryExecution queryExName = QueryExecutionFactory.create(query, model);
    ResultSet resultsName = queryExName.execSelect();

    QueryExecution queryExConvey = QueryExecutionFactory.create(query, model);
    ResultSet resultsConvey = queryExConvey.execSelect();

    QueryExecution queryExCounter = QueryExecutionFactory.create(query, model);
    ResultSet resultsCounter = queryExCounter.execSelect();

    int counter = 0;
    while (resultsCounter.hasNext()) {
      counter++;
      resultsCounter.next();
    }

    //Creater the stringVector with the according size
    String[][] stringVec = new String[counter][2];

    //Fills the stringVector with the movie name
    int i = 0;
    while (resultsName.hasNext()) {
      String strName = resultsName.next().get("IsCritiqueOf").toString();
      stringVec[i][0] = strName;
      i++;
    }

    //Fills the stringVector with the convey
    int j = 0;
    while (resultsConvey.hasNext()) {
      String strConvey = resultsConvey.next().get("Conveys").toString();
      stringVec[j][1] = strConvey;
      j++;
    }

    return stringVec;
  }

  //Sets table model to override the isCellEditable method so it won't let you edit rows
  private DefaultTableModel setTableModel(Query query, Model model, String[] columns) {

    DefaultTableModel tableModel = new DefaultTableModel(getTableRows(query, model, "title"), columns) {

      @Override
      public boolean isCellEditable(int row, int column) {
        //all cells false
        return false;
      }
    };

    return tableModel;
  }

  //Open the browser according to the link parameter
  private void openBrowser(URI link) {
    if (Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop().browse(link);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  //List of recommendation
  private List recommendationList(Query query, Model model, String parameter) {
    QueryExecution queryEx = QueryExecutionFactory.create(query, model);
    ResultSet results = queryEx.execSelect();

    // Fetch each row from the result set
    List list = new List();
    while (results.hasNext()) {
      String str = results.next().get(parameter).toString();

      list.add(str);
    }

    return list;
  }

  //Fills table rows
  private String[][] getTableRows(Query query, Model model, String parameter) {

    QueryExecution queryEx = QueryExecutionFactory.create(query, model);
    ResultSet results = queryEx.execSelect();

    // Fetch each row from the result set
    List list = new List();
    while (results.hasNext()) {
      String str = results.next().get(parameter).toString();

      list.add(str);
    }

    String[][] rows = new String[list.getItemCount()][1];

    int i = 0;
    while (i < list.getItemCount()) {
      rows[i][0] = list.getItem(i);
      i++;
    }

    return rows;
  }

  //Gets information from the first item of the query according to parameter
  private String getInformation(Query query, Model model, String information) {
    QueryExecution queryEx = QueryExecutionFactory.create(query, model);
    ResultSet results = queryEx.execSelect();

    try {
      return results.next().get(information).toString();
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  //Resize the poster image
  private static BufferedImage resize(BufferedImage img, int height, int width) {
    Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = resized.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();
    return resized;
  }

  //Return the ImageIcon for JLabel (download the poster)
  private ImageIcon downloadPoster(String imageUrl, String fileName) {

    //if imageUrl is null or N/A then there is nothing to download
    if (imageUrl == (null) || imageUrl.equals("N/A")) {
      return null;
    }

    //Tries to download the poster by url
    URL url = null;
    try {
      url = new URL(imageUrl);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    BufferedImage img = null;
    BufferedImage resized = null;
    try {
      img = ImageIO.read(url);
      resized = resize(img, 220, 150);
    } catch (IOException e) {
      return null;
    }
    File file = new File("/Users/leo/Desktop/TrabalhoIA-Filmes/src/posters/" + fileName + ".jpg");
    try {
      ImageIO.write(resized, "jpg", file);
    } catch (IOException e) {
      e.printStackTrace();
    }
    ImageIcon icon = null;
    icon = new ImageIcon("/Users/leo/Desktop/TrabalhoIA-Filmes/src/posters/" + fileName + ".jpg");
    return icon;
  }

  //Creates combo options
  private String[] createOptions() {
    String[] comboOptions = {"Actor", "Director", "Genre", "Movie Name"};
    return comboOptions;
  }

  //Creates component's layouts
  void createLayout(JComponent component, JFrame componentBackground, int sizeX, int sizeY, int locX, int locY) {
    //Size
    component.setSize(sizeX, sizeY);

    //Position
    component.setLocation(locX, locY);

    //Adds component to JFrame
    componentBackground.add(component);
  }

  //Updates the label according to the combo option selected
  private void updateLabel(JLabel label, int index) {
    switch (index) {
      case 0:
        label.setText(label.getText().replace("*", "actor name"));
        break;
      case 1:
        label.setText(label.getText().replace("*", "director name"));
        break;
      case 2:
        label.setText(label.getText().replace("*", "genre of the movie"));
        break;
      case 3:
        label.setText(label.getText().replace("*", "movie name"));
        break;
    }
  }

  //Creates the query according to the combo option selected
  private String buildQueryOMDB(int index, String parameter) {
    switch (index) {
      case 0:
        return
            "PREFIX topMovies: <http://www.semanticweb.org/manohara/ontologies/2016/9/untitled-ontology-10#> " +
                "SELECT ?title ?actor ?director ?genre ?poster ?year ?language " +
                "WHERE {" +
                "      ?resource topMovies:FallsUnderGenre ?genre . " +
                "      ?resource topMovies:HasTitle ?title . " +
                "      ?resource topMovies:HasDirector ?director . " +
                "      ?resource topMovies:HasPoster ?poster . " +
                "      ?resource topMovies:HasActor ?actor . " +
                "      ?resource topMovies:InYear ?year . " +
                "      ?resource topMovies:Language ?language . " +
                "      filter contains(?actor, " + "\"" + parameter + "\"" + ")" +
                "      }";
      case 1:
        return
            "PREFIX topMovies: <http://www.semanticweb.org/manohara/ontologies/2016/9/untitled-ontology-10#> " +
                "SELECT ?title ?actor ?director ?genre ?poster ?year ?language " +
                "WHERE {" +
                "      ?resource topMovies:FallsUnderGenre ?genre . " +
                "      ?resource topMovies:HasTitle ?title . " +
                "      ?resource topMovies:HasDirector ?director . " +
                "      ?resource topMovies:HasPoster ?poster . " +
                "      ?resource topMovies:HasActor ?actor . " +
                "      ?resource topMovies:InYear ?year . " +
                "      ?resource topMovies:Language ?language . " +
                "      filter contains(?director, " + "\"" + parameter + "\"" + ")" +
                "      }";
      case 2:
        return
            "PREFIX topMovies: <http://www.semanticweb.org/manohara/ontologies/2016/9/untitled-ontology-10#> " +
                "SELECT ?title ?actor ?director ?genre ?poster ?year ?language " +
                "WHERE {" +
                "      ?resource topMovies:FallsUnderGenre ?genre . " +
                "      ?resource topMovies:HasTitle ?title . " +
                "      ?resource topMovies:HasDirector ?director . " +
                "      ?resource topMovies:HasPoster ?poster . " +
                "      ?resource topMovies:HasActor ?actor . " +
                "      ?resource topMovies:InYear ?year . " +
                "      ?resource topMovies:Language ?language . " +
                "      filter contains(?genre, " + "\"" + parameter + "\"" + ")" +
                "      }";
      case 3:
        return
            "PREFIX topMovies: <http://www.semanticweb.org/manohara/ontologies/2016/9/untitled-ontology-10#> " +
                "SELECT ?title ?actor ?director ?genre ?poster ?year ?language " +
                "WHERE {" +
                "      ?resource topMovies:FallsUnderGenre ?genre . " +
                "      ?resource topMovies:HasTitle ?title . " +
                "      ?resource topMovies:HasDirector ?director . " +
                "      ?resource topMovies:HasPoster ?poster . " +
                "      ?resource topMovies:HasActor ?actor . " +
                "      ?resource topMovies:InYear ?year . " +
                "      ?resource topMovies:Language ?language . " +
                "      filter contains(?title, " + "\"" + parameter + "\"" + ")" +
                "      }";

    }

    return "";
  }

  //Creates the query according to the movie name parameter
  private String buildQueryNYTIMES(String movieName) {
    return
        "PREFIX topMovies: <http://www.semanticweb.org/manohara/ontologies/2016/9/untitled-ontology-10#> " +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT ?name ?IsCritiqueOf ?Conveys " +
            "WHERE {" +
            "      ?resource topMovies:IsCritiqueOf ?IsCritiqueOf . " +
            "      ?resource foaf:name ?name . " +
            "      ?resource topMovies:Conveys ?Conveys . " +
            "      filter contains(?IsCritiqueOf, " + "\"" + movieName + "\"" + ")" +
            "      }";
  }

  //Creates model according to rdf file
  private Model createModel(String rdfAdress) throws IOException {
    Model model = ModelFactory.createDefaultModel();

    Files.walkFileTree(Paths.get(rdfAdress), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        model.read(file.toUri().toString());
        return super.visitFile(file, attrs);
      }
    });

    return model;
  }

  public void initUI() throws IOException {

    //Creation of model NYTIMES
    final Model modelNYTIMES = createModel("/Users/leo/Desktop/TrabalhoIA-Filmes/Ontologies/nyTimes.rdf");

    //Creation of model OMDB
    final Model modelOMDB = createModel("/Users/leo/Desktop/TrabalhoIA-Filmes/Ontologies/omdb.rdf");

    //Creation of model OMDBTable
    final Model modelOMDBTable = createModel("/Users/leo/Desktop/TrabalhoIA-Filmes/Ontologies/omdb.rdf");

    this.setLayout(null);

    //Creation of label Search
    final JLabel searchLabel = new JLabel("Enter a parameter to start:");
    searchLabel.setFont(new Font("Courier New", Font.ITALIC, 13));
    searchLabel.setForeground(Color.GRAY);
    createLayout(searchLabel, this, 250, 50, 10, 5);

    //Creation of JComboBox Search already with all options
    final JComboBox searchCombo = new JComboBox(createOptions());
    createLayout(searchCombo, this, 150, 20, 5, 45);

    //Creation of label Input
    final JLabel inputLabel = new JLabel("Enter the *:");
    inputLabel.setFont(new Font("Courier New", Font.ITALIC, 13));
    inputLabel.setForeground(Color.GRAY);

    //Creation of JTextField InputTextField
    final JTextField inputTextField = new JTextField();

    //Creation of JButton Go
    JButton goButton = new JButton("Go");

    //Creation of the Box information
    Box information = Box.createVerticalBox();

    //Event when combo is clicked
    searchCombo.addActionListener((ActionEvent event) -> {

      //Disable the combo
      searchCombo.setEnabled(false);

      //Updates the label according to the combo option selected
      updateLabel(inputLabel, searchCombo.getSelectedIndex());

      //Creates components layouts and update the JFrame
      createLayout(inputLabel, this, 250, 50, 10, 65);
      createLayout(inputTextField, this, 150, 20, 5, 105);
      createLayout(goButton, this, 100, 30, 150, 101);

      //Focuses the inputTextField
      inputTextField.requestFocusInWindow();

      this.repaint();
    });

    //Event when button is clicked
    goButton.addActionListener((ActionEvent event) -> {

      //Disable the button (for now the user has to close and reopen)
      goButton.setEnabled(false);

      //Disable the input (for now the user has to close and reopen)
      inputTextField.setEnabled(false);

      //Saves the inputTextField value into input
      String input = inputTextField.getText().isEmpty() ? null : inputTextField.getText();

      //Build and mount the query of OMDB according to combo option selected and input
      Query queryOMDB = QueryFactory.create(buildQueryOMDB(searchCombo.getSelectedIndex(), input));

      //Build and mount the query of NYTIMES according to the director of the first recommendation
      Query queryNYTIMES = QueryFactory.create(buildQueryNYTIMES(getInformation(queryOMDB, modelOMDB, "title")));

      //Creation of JLabel poster
      JLabel poster = new JLabel();

      //Sets the image into the poster label and sets the size if there is one
      if (downloadPoster(getInformation(queryOMDB, modelOMDB, "poster"), "poster") == null) {
        poster.setText("No poster avaiable");
      } else {
        poster.setIcon(downloadPoster(getInformation(queryOMDB, modelOMDB, "poster"), "poster"));
      }

      //Initially conveyLink is null, but now we have the link
      conveyLink = getInformation(queryNYTIMES, modelNYTIMES, "Conveys");

      //Creation of title's label
      JLabel title = new JLabel(getInformation(queryOMDB, modelOMDB, "title"));
      title.setFont(new Font("Courier New", Font.ITALIC, 13));
      title.setForeground(Color.GRAY);
      createLayout(title, this, 150, 50, 5, 125);

      //Creation of the JLabel year and insertion in the Box information
      JLabel year = new JLabel("<html>Year: " + getInformation(queryOMDB, modelOMDB, "year") + "</html>");
      year.setSize(250, 50);
      year.setFont(new Font("Courier New", Font.ITALIC, 13));
      information.add(year);

      //Creation of the JLabel actors and insertion in the Box information
      JLabel actors = new JLabel("<html>Actors: " + getInformation(queryOMDB, modelOMDB, "actor") + "</html>");
      actors.setSize(250, 50);
      actors.setFont(new Font("Courier New", Font.ITALIC, 13));
      information.add(actors);

      //Creation of the JLabel director and insertion in the Box information
      JLabel director = new JLabel("<html>Director: " + getInformation(queryOMDB, modelOMDB, "director") + "</html>");
      director.setSize(250, 50);
      director.setFont(new Font("Courier New", Font.ITALIC, 13));
      information.add(director);

      //Creation of the JLabel genre and insertion in the Box information
      JLabel genre = new JLabel("<html>Genre: " + getInformation(queryOMDB, modelOMDB, "genre") + "</html>");
      genre.setSize(250, 50);
      genre.setFont(new Font("Courier New", Font.ITALIC, 13));
      information.add(genre);

      //Creation of the JLabel language and insertion in the Box information
      JLabel language = new JLabel("<html>Language: " + getInformation(queryOMDB, modelOMDB, "language") + "</html>");
      language.setSize(250, 50);
      language.setFont(new Font("Courier New", Font.ITALIC, 13));
      information.add(language);

      //Creation of panel to insert the poster image
      JPanel panelImage = new JPanel();
      createLayout(panelImage, this, 155, 225, 5, 160);

      //Enable mouse interface clickable on the poster and sets the event (opens the browser)
      poster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      poster.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          URI link = null;

          //conveyLink is null so there is no link to open in browser
          if (conveyLink != null) {
            try {
              link = new URI(conveyLink);
              openBrowser(link);
            } catch (URISyntaxException ex) {
              ex.printStackTrace();
            }
          }
        }
      });

      //Adds the poster label to the panel
      panelImage.add(poster);

      //Updates the panel (do not remove)
      panelImage.revalidate();

      //Creation of You may like's label
      JLabel youMayLike = new JLabel("You may like:");
      youMayLike.setFont(new Font("Courier New", Font.ITALIC, 13));
      youMayLike.setForeground(Color.GRAY);
      createLayout(youMayLike, this, 200, 50, 190, 125);

      //Creation of recommendation panel
      JPanel panelRecommendation = new JPanel();
      createLayout(panelRecommendation, this, 155, 225, 160, 160);

      //Build and mount the query of OMDBTable according to combo option selected and input
      Query queryOMDBTable = QueryFactory.create(buildQueryOMDB(searchCombo.getSelectedIndex(), input));

      //Creates the column
      String[] columns = {""};

      //Creates the table
      JTable table = new JTable();

      //Set the size of table and the model not editable (do not remove)
      table.setPreferredSize(new Dimension(155, 225));
      table.setModel(setTableModel(queryOMDBTable, modelOMDBTable, columns));

      //Adds the table to panelRecommendation
      panelRecommendation.add(table);

      //Event when table is clicked
      table.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {

          //Clears the old title so it won't be written above
          title.setText(null);

          //Gets row and column clicked
          int row = table.rowAtPoint(evt.getPoint());
          int col = table.columnAtPoint(evt.getPoint());
          List listTitle = recommendationList(queryOMDBTable, modelOMDBTable, "title");
          List listPoster = recommendationList(queryOMDBTable, modelOMDBTable, "poster");
          List listYear = recommendationList(queryOMDBTable, modelOMDBTable, "year");
          List listActor = recommendationList(queryOMDBTable, modelOMDBTable, "actor");
          List listDirector = recommendationList(queryOMDBTable, modelOMDBTable, "director");
          List listGenre = recommendationList(queryOMDBTable, modelOMDBTable, "genre");
          List listLanguage = recommendationList(queryOMDBTable, modelOMDBTable, "language");

          //Build and mount the query of NYTIMES to get all data
          Query queryNYTIMES = QueryFactory.create(buildQueryNYTIMESALL());

          //Gets a String[][] with the according name and convey of the movie
          String[][] strVecNameConvey = getNameAndConveyNYTIMESALL(queryNYTIMES, modelNYTIMES);

          if (row >= 0 && col >= 0) {

            //Updates the labels according to the movie's name on the row clicked
            year.setText("<html>Year: " + listYear.getItem(row) + "</html>");
            actors.setText("<html>Actor(s): " + listActor.getItem(row) + "</html>");
            director.setText("<html>Director(s): " + listDirector.getItem(row) + "</html>");
            genre.setText("<html>Genre(s): " + listGenre.getItem(row) + "</html>");
            language.setText("<html>Language(s): " + listLanguage.getItem(row) + "</html>");
            title.setText(listTitle.getItem(row));

            //Gets the conveyLink
            conveyLink = getConvey(strVecNameConvey, listTitle.getItem(row));

            //Updates poster's image if there is a link in the rdf file
            poster.setText(null);
            if (listPoster.getItem(row).contains("N/A") || downloadPoster(listPoster.getItem(row), "updatedPoster" + row) == null) {
              poster.setText("No poster avaiable");
              poster.setIcon(null);
            } else {
              //Everything is fine so shows the poster
              poster.setIcon(downloadPoster(listPoster.getItem(row), "updatedPoster" + row));
            }

            //Repaints the UI (do not remove)
            initUI.super.repaint();
          }
        }
      });

      //Bordering and insertion in the Frame of the information Box
      information.setBorder(BorderFactory.createTitledBorder("Information"));

      createLayout(information, this, 315, 140, 5, 385);

      this.repaint();
    });

    ////////////////// USER INTERFACE //////////////////

    setVisible(true);

    setResizable(false);

    setTitle("Movie Recommender");

    setSize(320, 550);

    setLocationRelativeTo(null);

    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }
}