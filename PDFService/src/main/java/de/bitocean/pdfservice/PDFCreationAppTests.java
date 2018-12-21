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
 * @author kamir
 */
public class PDFCreationAppTests {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
        String[] args1 = { "Hallo Mirko!",  "File1.pdf" };
        createPDF( args1 );
        
 //     String[] args2 = { "Urkunde2.pdf",  "File2.pdf" };
 //     String[] args2 = { "Test4.pdf",  "Urkunde4.pdf", "Text Box 1", "Der Verein", "Text Box 2", "08-15 und 17u4", "Text Box 3", "Gestern Abend ..."  };
        String[] args2 = { "Test4.pdf",  "Urkunde4.pdf", "Organisation", "Der Verein", "Nummer", "08-15 oder 17und4", "Datum", "2015-05-05"  };
 //     String[] args2 = { "Zertifikat_Tectum_namen_pub_0001.pdf",  "File3.pdf", "Organisation", "A", "Nummer", "B", "Datum", "C" };
        setFields( args2 );
        
    }
    
    
    
    public static void createPDF( String[] args ) throws IOException, COSVisitorException {
      String meinText   = ( args.length > 0 ) ? args[0] : "Hallo Mirko!";
      String outputFile = ( args.length > 1 ) ? args[1] : "File1.pdf";

      PDDocument pdDoc  = new PDDocument();  
      PDPage     page   = new PDPage();
      pdDoc.addPage( page );
      
      PDPageContentStream content = new PDPageContentStream( pdDoc, page );
      content.beginText();
      content.setFont( PDType1Font.HELVETICA_BOLD, 12 );
      content.moveTextPositionByAmount( 100, 700 );
      content.drawString( meinText );
      content.endText();
      content.close();
      pdDoc.save( outputFile );
      pdDoc.close();
      
   }
    
    
    
   public static void setFields( String[] args ) throws IOException, COSVisitorException
   {
      System.out.println(
            "\nSetzt Werte in die Formularfelder eines PDF-Dokuments ein (oder listet die vorhandenen Formularfelder auf)." +
            "\nAufruf mit den Parametern: InputPdfFile OutputPdfFile Name1 Value1 Name2 Value2 ...\n" );
      String   inputPdfFile   = (args.length > 0) ? args[0] : null;
      String   outputPdfFile  = (args.length > 1) ? args[1] : null;
      String[] nameValuePairs = (args.length > 2) ? Arrays.copyOfRange( args, 2, args.length ) : null;
      setFields( inputPdfFile, outputPdfFile, nameValuePairs );
   }

   public static void setFields( String inputPdfFile, String outputPdfFile, String[] nameValuePairs )
         throws IOException, COSVisitorException
   {
      if( inputPdfFile == null ) { return; }
      
      PDDocument        pdDoc = PDDocument.load( new File( inputPdfFile ) );
      PDDocumentCatalog pdCat = pdDoc.getDocumentCatalog();
      PDAcroForm        acroForm = pdCat.getAcroForm();
       
      Set<Entry<COSName,COSBase>> set = acroForm.getDictionary().entrySet();
      for( Entry e: set ) {
          System.out.println( "> " + e );
          
          if ( e.getKey().equals(COSName.FIELDS) ) {
             System.out.println( e );
             COSArray a = (COSArray)e.getValue();
             Iterator it = a.iterator();
             while( it.hasNext() ) {
                 
                 COSObject o = (COSObject) it.next();
                 System.out.println( o.getDictionaryObject( COSName.FIELDS ) );
             }
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
      
      pdDoc.save( outputPdfFile );
      pdDoc.close();
   }

   public static void setField( PDAcroForm acroForm, String name, String value ) throws IOException
   {
      PDField field = acroForm.getField( name );
      
      
      if( field != null ) {
         field.setValue( value );
      } else {
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
