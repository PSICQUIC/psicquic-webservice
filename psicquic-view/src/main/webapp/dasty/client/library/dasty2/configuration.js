// JavaScript Document

/**
*  CONFIGURATION VARIABLES
*/

function configuration()
{
	/**
	*  TEMPLATES VARIABLES
	*/
	if(dasty_mainpage_name == "ebi.php")
		{
			/**
			* VARIABLES FOR THE EBI TEMPLATE
			*/
				isPDBVisible=true;
				isOntologyTreeVisible=true;
				
				sequence_limit = 110; // Define the "number" of aa per line in the sequence view. 75
				
				graphic_width = 1200;
				height_graphic_feature = 7; // Height for the features shown in the graphic.
				tittle_height = height_graphic_feature + 6;
				
				col_category_width = 170;
				col_type_width = 200;
				col_id_width = 110;
				col_warning_width = 26;
				col_server_width = 140;
		
				// 0=no / 1=yes
				show_col_category = 1; 
				show_col_type = 1;
				show_col_id = 1;
				show_col_graphic = 1;
				show_col_warning = 1;
				show_col_server = 1;
				
				// 0=no / 1=yes
				show_graphic_tittle = 1;
				show_scale_bar = 1;
				show_slide_bar =1;
				show_popup =2;
				
				// boolean 0/1. 0=no / 1=yes
				color_line_background = 0;
				
				vertical_bars = 12;
				
				// Do you want to exclude DAS sources from the visuzlization?
				//excluded_das_sources = []; // No
				//excluded_das_sources = ['netphos', 'tmhmm']; // Yes, Netphos and Tmhmm
				excluded_das_sources = ['CATH Structural Domains in UniProt', 'cath_uniprot_mapping'];
				
				
				// Do you want to load at the beggining one specific DAS source?
				first_das_source = []; // No
				//first_das_source = ['pride', 'uniprot']; // Yes, Pride and Uniprot following this order.
				
				non_positional_features_coulmns = ["type_id", "method_data", "score_data", "link_data", "feature_id", "note_data", "annotation_server", "type_category"];
				
				non_positional_feature_table_width= "98%";
				//non_positional_feature_table_width= "900px";
				
				use_das_registry = 1;
				
		}
	else if(dasty_mainpage_name == "uniprot.php")
		{
			/**
			* VARIABLES FOR THE UNIPROT BETA TEMPLATE
			*/
				isPDBVisible=true;
				isOntologyTreeVisible=true;
				
				sequence_limit = 110; // Define the "number" of aa per line in the sequence view. 75
				
				graphic_width = 770;
				height_graphic_feature = 7; // Height for the features shown in the graphic.
				tittle_height = height_graphic_feature + 6;
				
				col_category_width = 170;
				col_type_width = 200;
				col_id_width = 110;
				col_warning_width = 26;
				col_server_width = 140;
		
				// boolean 0/1. 0=no / 1=yes
				show_col_category = 0; 
				show_col_type = 1;
				show_col_id = 1;
				show_col_graphic = 1;
				show_col_warning = 1;
				show_col_server = 1;
				
				// boolean 0/1. 0=no / 1=yes
				show_graphic_tittle = 1;
				show_scale_bar = 1;
				show_slide_bar =1;
				show_popup =2;
				
				// boolean 0/1. 0=no / 1=yes
				color_line_background = 0;
				
				vertical_bars = 12;
				
				// Do you want to exclude DAS sources from the visuzlization?
				//excluded_das_sources = []; // No
				//excluded_das_sources = ['netphos', 'tmhmm']; // Yes, Netphos and Tmhmm
				excluded_das_sources = ['uniprot aristotle', 'uniprot'];
				
				
				// Do you want to load at the beggining one specific DAS source?
				first_das_source = []; // No
				//first_das_source = ['pride', 'uniprot']; // Yes, Pride and Uniprot following this order.
				//first_das_source = ['signal'];
				
				non_positional_features_coulmns = ["type_id", "score_data", "method_data", "link_data", "feature_id", "note_data", "annotation_server", "type_category"];
				
				non_positional_feature_table_width= "98%";
				//non_positional_feature_table_width= "900px";
				
				use_das_registry = 1;
				
		}
	else if(dasty_mainpage_name == "biosapiens.html")
		{
			/**
			* VARIABLES FOR THE BIOSAPIENS TEMPLATE
			*/
				isPDBVisible=true;
				isOntologyTreeVisible=true;
				
				sequence_limit = 110; // Define the "number" of aa per line in the sequence view. 75
				
				graphic_width = 900;
				height_graphic_feature = 7; // Height for the features shown in the graphic.
				tittle_height = height_graphic_feature + 6;
				
				col_category_width = 170;
				col_type_width = 200;
				col_id_width = 110;
				col_warning_width = 26;
				col_server_width = 140;
		
				// boolean 0/1. 0=no / 1=yes
				show_col_category = 0; 
				show_col_type = 1;
				show_col_id = 0;
				show_col_graphic = 1;
				show_col_warning = 1;
				show_col_server = 1;
				
				// boolean 0/1. 0=no / 1=yes
				show_graphic_tittle = 1;
				show_scale_bar = 1;
				show_slide_bar =1;
				show_popup =2;
				
				// boolean 0/1. 0=no / 1=yes
				color_line_background = 0;
				
				vertical_bars = 8;
				
				// Do you want to exclude DAS sources from the visuzlization?
				excluded_das_sources = []; // No
				//excluded_das_sources = ['netphos', 'tmhmm']; // Yes, Netphos and Tmhmm
				//excluded_das_sources = ['CATH Structural Domains in UniProt', 'cath_uniprot_mapping'];
				
				
				// Do you want to load at the beggining one specific DAS source?
				first_das_source = []; // No
				//first_das_source = ['pride', 'uniprot']; // Yes, Pride and Uniprot following this order.
				//first_das_source = ['signal'];			
				
				non_positional_features_coulmns = ["type_id", "method_data", "score_data", "link_data", "feature_id", "note_data", "annotation_server", "type_category"];
				
				non_positional_feature_table_width= "98%";
				//non_positional_feature_table_width= "900px";
				
				use_das_registry = 1;
		}
    else
		{
			/**
			* VARIABLES FOR OTHER TEMPLATES. EXAMPLE: INTERACTOR VIEW
			*/
				isPDBVisible=false;
				isOntologyTreeVisible=true;
				
				sequence_limit = 110; // Define the "number" of aa per line in the sequence view. 75
				
				graphic_width = 900;
				height_graphic_feature = 7; // Height for the features shown in the graphic.
				tittle_height = height_graphic_feature + 6;
				
				col_category_width = 170;
				col_type_width = 200;
				col_id_width = 110;
				col_warning_width = 26;
				col_server_width = 140;
		
				// boolean 0/1. 0=no / 1=yes
				show_col_category = 0; 
				show_col_type = 1;
				show_col_id = 0;
				show_col_graphic = 1;
				show_col_warning = 1;
				show_col_server = 1;
				
				// boolean 0/1. 0=no / 1=yes
				show_graphic_tittle = 1;
				show_scale_bar = 1;
				show_slide_bar =1;
				show_popup =1;
				
				// boolean 0/1. 0=no / 1=yes
				color_line_background = 0;
				
				vertical_bars = 12;
				
				// Do you want to exclude DAS sources from the visuzlization?
				//excluded_das_sources = []; // No
				//excluded_das_sources = ['netphos', 'tmhmm']; // Yes, Netphos and Tmhmm
				excluded_das_sources = ['CATH Structural Domains in UniProt', 'cath_uniprot_mapping'];
				
				
				// Do you want to load at the beggining one specific DAS source?
				first_das_source = []; // No
				//first_das_source = ['pride', 'uniprot']; // Yes, Pride and Uniprot following this order.
				//first_das_source = ['signal'];			
				
				non_positional_features_coulmns = ["type_id", "link_data", "note_data", "annotation_server"];

				non_positional_feature_table_width= "98%";
				//non_positional_feature_table_width= "900px";	
				
				use_das_registry = 0;
		}
	
	/**
	* DAS SOURCES VARIALES
	*/
		stylesheet_url = [];
		sequence_url = [];
		feature_url = [];
		
		/**
		* PROXY
		*/
			/**
			* PHP PROXY
			*/
				//proxy_url = '../server/proxy.php';
			/**
			* CGI PROXY
			*/
				//proxy_url = 'http://www.ebi.ac.uk/cgi-bin/dasty/proxy.cgi';
				//proxy_url = 'http://www.ebi.ac.uk/~rafael/cgi-bin/proxy.cgi';
				//proxy_url = 'http://localhost/cgi-bin/proxy.cgi';
				//proxy_url = 'http://wwwdev.ebi.ac.uk/cgi-bin/dasty/proxy.cgi';
					

			/* Java PROXY
			*/
				proxy_url = './das.dasProxy';

        /**
		* STYLESHEET
		*/		
			stylesheet_url[0] = ['uniprot', proxy_url + '?t=' + timeout + '&m=stylesheet&s=http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle/'];
			//stylesheet_url[0] = ['uniprot', 'files/stylesheet.xml']; // LOCAL STYLESHEET

		/**
		* REFERENCE SERVER
		*/		
			sequence_url[0] = ['intact', proxy_url + '?t=' + timeout + '&m=sequence&q=' + query_id + '&s=${das.referenceserver.url}']; // INTACT REFERENCE SERVER
			//sequence_url[0] = ['uniprot', 'files/seq_A4_Human_uniprot02.xml']; // LOCAL SEQUENCE (just for testing purposes)
					
		/**
		* ANNOTATION SERVERS
		*  - If use_das_registry = 0 please Set specific DAS annotation servers
		*/	
			
			if(use_das_registry == 1)
				{
					/**
					* ANNOTATIONS FROM THE DAS REGISTRY
					*/	
						das_registry_url = proxy_url + '?t=' + timeout + '&m=registry&c=protein%20sequence&a=UniProt&s=http://www.dasregistry.org/das1/sources';
						//das_registry_url = proxy_url + '?m=registry&s=http://das.sanger.ac.uk/registry/das1/sources/'; 
						//das_registry_url = 'files/dasregistry050907.xml'; // LOCAL REGISTRY
							
							// Exceptions. For example ...
//							if(dasty_mainpage_name.indexOf("interactor") != -1)
//								{
//									das_registry_url = 'files/dasregistry020208.xml';
//								}
				}
			else
				{
					/**
					* SPECIFIC DAS ANNOTATION SERVERS
					*/	
						feature_url_prefix = proxy_url + '?m=features&q=' + query_id + '&t=' + timeout + '&s=';
                    feature_url[0] = {id : 'uniprot', url : feature_url_prefix + 'http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle/'};
                    feature_url[1] = {id : 'interpro', url : feature_url_prefix + 'http://das.ensembl.org/das/interpro/'};
                    feature_url[2] = {id : 'intact', url : feature_url_prefix + '${das.annotationserver.intact.url}'};
                    //feature_url[3] = {id : 'chebi', url : feature_url_prefix + '${das.annotationserver.chebi.url}'};
						//feature_url[1] = {id : 'msdmotif', url : feature_url_prefix + 'http://www.ebi.ac.uk/msd-srv/msdmotif/das/s3dm/'};
						//feature_url[2] = {id : 'netphos', url : feature_url_prefix + 'http://genome.cbs.dtu.dk:9000/das/netphos/'};
						//feature_url[3] = {id : 'uniprot2', url : feature_url_prefix + 'http://tc-test-1.ebi.ac.uk:8113/tc-test/proteomics/das-srv/uniprot/das/uniprot/'};

					/**
					* SPECIFIC DAS ANNOTATION SERVERS. LOCAL COPIES.
					*/
                        //feature_url[0] = {id : 'uniprot', url : 'files/fea_A4_Human_uniprot03.xml'};
						//feature_url[1] = {id : 'cbs_total', url : 'files/fea_A4_Human_cbs_total.xml'};
						//feature_url[2] = {id : 'netphos', url : 'files/fea_A4_Human_netphos.xml'};
						//feature_url[3] = {id : 'intact', url : 'files/intact_EBI-466029.xml'};
				}
		
			/**
			* ALIGNMENTS
			*/	
					uniprot_pdb_alignment = proxy_url + '?t=30&m=alignment&q=' + query_id + '&s=http://das.sanger.ac.uk/das/msdpdbsp/';
				
			/**
			* ONTOLOGY
			*/	
				onto_path_type = proxy_url + '?t=10&m=ontology&q=biosapiens_lite.xml&s=' + dasty_path + '../ontology/';
				onto_path_category = proxy_url + '?t=10&m=ontology&q=categories.xml&s=' + dasty_path + '../ontology/';
				//onto_path_type = proxy_url + '?t=30&m=ontology&q=biosapiens.obo_types.xml&s=http://localhost/dasty2/server/pdb/';
				//onto_path_category = proxy_url + '?t=30&m=ontology&q=categories.xml&s=http://localhost/dasty2/server/pdb/';

			
			/**
			* Path to load pdb files
			*/	
				path_pdb_files="../server/pdb/";
}


/**
* DEFAULT QUERY PARAMETERS
*/	
function default_query_parameters()
{	
	//default_query_id = "P05067";
	default_query_id = "";
	default_filterLabel = "BioSapiens";
	default_timeout = 3;
	default_dasty_mainpage_name = "interactorview.xhtml"
}
