package ca.openquiz.webservice.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import ca.openquiz.webservice.enums.Degree;
import ca.openquiz.webservice.enums.Language;
import ca.openquiz.webservice.enums.QuestionType;
import ca.openquiz.webservice.model.Choice;
import ca.openquiz.webservice.model.Question;
import ca.openquiz.webservice.model.QuestionAnagram;
import ca.openquiz.webservice.model.QuestionAssociation;
import ca.openquiz.webservice.model.QuestionGeneral;
import ca.openquiz.webservice.model.QuestionIdentification;
import ca.openquiz.webservice.model.QuestionIntru;
import ca.openquiz.webservice.response.KeyResponse;


public class ImportTest {
	
	public static void extractCSV(String file, QuestionType questionType,
			String author, List<String> groups, Language language) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			String tokens[];

			// We skip the first line because its the header
			line = br.readLine();
			Question currentQuestion = null;
			
			System.out.println("Starting to import questions " + QuestionType.Anagram.toString());
			while (line != null) {
				tokens = line.split("\t");
				currentQuestion = null;

				try {
					switch (questionType) {
					case Anagram:
						currentQuestion = parseQuestionAnagram(tokens);
						break;
					case Association:
						currentQuestion = parseQuestionAssociation(tokens);
						break;
					case General:
						currentQuestion = parseQuestionGeneral(tokens);
						break;
					case Identification:
						currentQuestion = parseQuestionIdentification(tokens);
						break;
					case Intru:
						currentQuestion = parseQuestionIntru(tokens);
						break;

					}
				} catch (Exception e) {}

				if (currentQuestion != null) {

					// Set the author, roles and all the info that will be the
					// same for each questions
					//currentQuestion.setAuthorKey(author);
					//currentQuestion.setGroupRolesList(groups);
					currentQuestion.setLanguage(language);
					currentQuestion.setCategory(null);
					// TODO Insert the new question to the db
					// TODO Add a way to specify username/password
					
					//KeyResponse key = RequestsWebService.verifyUserCredentials("mathieu.charpenel@gmail.com", "openquiz");
					/*KeyResponse key = RequestsWebService.addQuestion(currentQuestion,DatatypeConverter.printBase64Binary(("mathieu.charpenel@gmail.com:openquiz").getBytes()));
					if(key != null && key.getKey()!= null && !key.getKey().isEmpty()){
						System.out.println("Question added: " + line);
					}else{
						System.out.println("There was a problem adding to DB: " + line);
						
					}*/
					
					
				} else {
					System.out.println("There was a problem parsing line: "
							+ line);
				}
				line = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static QuestionAnagram parseQuestionAnagram(String[] tokens) {
		// EXEMPLE: 1 Termes math�matiques ADDITION 0 NO A D D I I N O T 5
		QuestionAnagram question = new QuestionAnagram();
		question.setHint(tokens[1]);
		question.setAnswer(tokens[2]);

		question.setDegree(getDegreeFromString(tokens[3]));

		question.setAnagram(tokens[5]);

		return question;
	}
	
	private static QuestionAssociation parseQuestionAssociation(String[] tokens) {
		// EXEMPLE : 1 Associez la capitale avec le pays. PARAGUAY HONDURAS EQUATEUR QUITO T�GUCIGALPA ASUNCION C B A 0 NO 77
		QuestionAssociation question = new QuestionAssociation();

		question.setQuestion(tokens[1]);
		List<Choice> choices = new LinkedList<Choice>();

		// Association du mot a la bonne reponse
		for (int i = 0; i < 3; i++) {
			String reponse = tokens[i + 8];
			Choice choice = new Choice();
			choice.setChoice(tokens[i + 2]);
			choice.setAnswer(tokens[5 + reponse.charAt(0) - 'A']);
			choices.add(choice);
		}

		question.setChoices(choices);
		question.setDegree(getDegreeFromString(tokens[11]));

		return question;
	}
	
	private static QuestionGeneral parseQuestionGeneral(String[] tokens) {
		// 		Question 									Reponse 	Difficulte 	Util_jeu 	Ancien no 		Dla marde
		// 1 	Le sixi�me cas de la d�clinaison latine. 	Ablatif 	2 			NO		 	4 				0

		QuestionGeneral question = new QuestionGeneral();
		question.setQuestion(removeDoubleQuotes( tokens[1]));
		question.setAnswer(tokens[2]);
		question.setDegree(getDegreeFromString(tokens[3]));

		return question;
	}

	private static QuestionIdentification parseQuestionIdentification(String[] tokens) {
		//		Indice 1				Indice 2																	Indice 3													Indice 4														Difficulte	Util_jeu	Reponse
		//	1	Voir deuxi�me indice.	"Cin�aste am�ricain, il est l`auteur de plusieurs films sur les animaux."	Il est surtout reconnu pour ses films en bandes dessin�es.	Il a cr�� les personnages de Mickey Mouse et de Donald Duck. 	0			NO			WALT DISNEY
		// On skip le premier indice parce que ya jamais rien la...
		QuestionIdentification question = new QuestionIdentification();
		List<String> indices = new LinkedList<String>();
		for(int i = 0; i < 3; i++){
			indices.add(removeDoubleQuotes(tokens[2+i]));
		}
		// question.setStatements(indices); 	# D�SUET
		question.setDegree(getDegreeFromString(tokens[5])); 
		question.setAnswer(tokens[7]);
		
 
		return question;
	}

	private static QuestionIntru parseQuestionIntru(String[] tokens) {
		//		Question								Mot 1	Mot 2	Mot 3	Mot 4	Reponse	Difficulte	Util_jeu	Dla marde
		//1		Laquelle n'est pas une constellation?	VIERGE	LION	MERLAN	CANCER	C		0			NO			18

		QuestionIntru question = new QuestionIntru();
		question.setQuestion(tokens[1]);
		List<String> choices = new LinkedList<String>();
		for(int i = 2 ; i < 6; i++){
			choices.add(tokens[i]);
		}
		//question.setChoices(choices);		# D�SUET
		question.setAnswer(tokens[2 + tokens[6].charAt(0) - 'A']);
		question.setDegree(getDegreeFromString(tokens[7]));
		
		return question;
	}

	private static Degree getDegreeFromString(String degree) {
		if (degree.equals("0")) {
			return Degree.easy;
		} else if (degree.equals("1")) {
			return Degree.normal;
		} else if (degree.equals("2")) {
			return Degree.hard;
		} else
			return Degree.normal;
	}
	
	private static String removeDoubleQuotes(String string) {
		if (string.startsWith("\"") && string.endsWith("\"")) {
			string = string.substring(1, string.length()-2).replace("\"\"", "\"");
		}
		return string;
	}

}