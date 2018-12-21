package de.bitocean.pdfservice;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry; 
import java.util.Set;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;


/**
 * The Tectum-PDF creator generates a set of PDF files.
 * 
 * 1.) Urkunde für Kunden
 * 2.) Urkunden-Gerüst und Adressaufkleber für Operator 2
 * 3.) Vertrag
 * 4.) Rechnung
 * 
 * @author kamir
 */
public class TectumPDFCreatorApp {
    
    String inputPdfFile = null;
    String outputPdfFile = null;
      
    public TectumPDFCreatorApp( String fnTemplate ) {
        
        inputPdfFile = fnTemplate;
        
        System.out.println(">>> Input Pfad für Template: " + fnTemplate);
       
        File f = new File(fnTemplate);
        
        // if ( !f.canRead() ) System.exit(0);
        
    }
    
    public void makePDF( String fnRESULTFILE, String[] texts ) throws IOException, COSVisitorException {
      
       outputPdfFile = fnRESULTFILE;
       
       System.out.println( "*****> " +  new File(outputPdfFile).getAbsolutePath() );
      
       setFields( texts );
    
    }
    
    /**
     * 
     * This is for Component testing only!
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        String templateFolder = "/home/CCHL.eu/templates/grad_v1/"; 
        String resultFolder = "/home/CCHL.eu/documents/"; 

        if ( args == null || args.length < 2 ) {
           
            templateFolder = "./templates/grad_v1/"; 
            resultFolder = "./documents/"; 

        }
        else {
            
         templateFolder = args[0];
         resultFolder = args[1];
         
        }
        
    

        String NR = "123456789";
        String[] data1 = {"Herr", "Mirko", "Nummer", "16. April 1975 in Stollberg (Erzg.)", "Datum", "2015-05-05"  };

        String[] data3 = {"Organisation", "Der Verein", "Strasse", "Am Dorfplatz 1208.x bis z", "PLZ", "06259", "Ort", "Frankleben", "Land", "Deutschland" };
        
        // --------------------- URKUNDE für Kunde (per Email) --------------
        TectumPDFCreatorApp c1 = new TectumPDFCreatorApp( templateFolder + "Urkunde_für_Kunde_TPLv1.pdf" );
        c1.makePDF(resultFolder + "Urkunde_Kunde_" + NR + ".pdf" , data1 );

        // --------------------- Ausdruck der Urkunde auf farbigem Template --------------
        TectumPDFCreatorApp c2 = new TectumPDFCreatorApp(  templateFolder + "Urkunde_für_DRUCKER_TPLv1.pdf" );
        c2.makePDF(resultFolder + "Urkunde_PRINTER_" + NR + ".pdf" , data1 );

        // --------------------- Adressaufkleber --------------
        TectumPDFCreatorApp c3 = new TectumPDFCreatorApp(  templateFolder + "Adresse_für_DRUCKER_TPLv1.pdf" );
        c3.makePDF(resultFolder + "Adresse_PRINTER_" + NR + ".pdf" , data3 );
        
        // --------------------- Vertrag --------------
        TectumPDFCreatorApp c4 = new TectumPDFCreatorApp(  templateFolder + "Vertrag_TPLv2.pdf" );
        c4.makePDF(resultFolder + "Vertrag__" + NR + ".pdf" , data3 );        
        
        // --------------------- Rechnung --------------
        TectumPDFCreatorApp c5 = new TectumPDFCreatorApp(  templateFolder + "Rechnung_TPLv1.pdf" );
        c5.makePDF(resultFolder + "Rechnung_" + NR + ".pdf" , data3 );       
    }
    
    
     

   public void setFields( String[] nameValuePairs )
         throws IOException, COSVisitorException
   {
      if( inputPdfFile == null ) { return; }
      
      File f = new File( inputPdfFile );
      System.out.println(">>> PDFService Loader: " + f.getAbsolutePath() );
      
      PDDocument        pdDoc = PDDocument.load( f );
      PDDocumentCatalog pdCat = pdDoc.getDocumentCatalog();
      PDAcroForm        acroForm = pdCat.getAcroForm();
       
      Set<Entry<COSName,COSBase>> set = acroForm.getDictionary().entrySet();
      for( Entry e: set ) {
          System.out.println( "> " + e );
          
          if ( e.getKey().equals(COSName.FIELDS) ) {
             
//             System.out.println( e );
//             
//             COSArray a = (COSArray)e.getValue();
//             Iterator it = a.iterator();
//             
//             while( it.hasNext() ) {
//                 
//                 COSObject o = (COSObject) it.next();
//                 
//                 System.out.println( o.getDictionaryObject( COSName.FIELDS ) );
//                 
//             }
          }
      }        
      
      if( acroForm == null ) {
         System.out.println( "Das Dokument '" + inputPdfFile + "' enthaelt kein PDF-Formular." );
         return;
      }
      
      if( outputPdfFile == null || nameValuePairs == null ) {
         printFieldNames( acroForm );
         return;
      }
      int i = 0;
      
      while( i < nameValuePairs.length - 1 ) {
         setField( acroForm, nameValuePairs[i++], nameValuePairs[i++] );
      }

      File f2 = new File( outputPdfFile );
      System.out.println(">>> PDFService Writer: " + f.getAbsolutePath() );

      pdDoc.save( f2.getAbsolutePath() );
      pdDoc.close();
      
   }

   public static void setField( PDAcroForm acroForm, String name, String value ) throws IOException
   {
      PDField field = acroForm.getField( name );
      
      if( field != null ) {
         field.setValue( value );
      } 
      else {
         System.err.println( "Es gibt kein Formularfeld mit dem Namen '" + name + "'." );
         printFieldNames( acroForm );
      }
      
   }

   public static void printFieldNames( PDAcroForm acroForm ) throws IOException
   {
      System.out.println( "Das PDF-Dokument enthaelt folgende Formularfelder:" );
      @SuppressWarnings("unchecked")
      List<PDField> fields = acroForm.getFields();
      for( PDField f : fields ) {
          
        System.out.println( f.getFullyQualifiedName() + " : " + f.getAlternateFieldName());
        
        
      }
   } 
    
}
