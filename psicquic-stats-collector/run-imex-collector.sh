mvn -X clean install -Pcollect-stats-imex -Dpsicquic.miql.query="(interaction_id:imex OR pubid:imex)" -Dpsicquic.registry.url="http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=STATUS&tags=%22imex%20curation%22&format=txt"

