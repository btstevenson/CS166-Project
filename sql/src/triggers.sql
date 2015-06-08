/* creates new msgID when user sends message */
CREATE SEQUENCE msgIDSequence;
SELECT setval('msgIDSequence', 27811);

/* searches for valid connection possibilities */
CREATE OR REPLACE FUNCTION conn_search(connName varchar) RETURNS VOID AS $$
DECLARE
	tempI connection_usr%rowtype;
	tempJ connection_usr%rowtype;
	tempK connection_usr%rowtype;
	tableName ALIAS FOR $1;
BEGIN 
		BEGIN
			RAISE NOTICE 'table name is: %', tableName;	
			EXECUTE 'CREATE TABLE '|| quote_ident(tableName) ||' (userId varchar(50))';
		EXCEPTION WHEN duplicate_table THEN

		END;
	FOR tempI IN SELECT connectionid AS userid FROM connection_usr WHERE userid = connName AND status = 'Accept' UNION ALL SELECT userid FROM connection_usr WHERE connectionid = connName AND status = 'Accept' LOOP
			FOR tempJ IN SELECT connectionid AS userid FROM connection_usr WHERE userid = tempI.userid AND connectionid <> connName AND status = 'Accept' UNION SELECT userid FROM connection_usr WHERE connectionid = tempI.userid AND userid <> connName AND status = 'Accept'  LOOP				  
				RAISE NOTICE 'TempJ is: %', tempJ.userid;
				EXECUTE 'INSERT INTO '|| quote_ident(tableName) ||' ' || 'VALUES (' || quote_literal(tempJ.userid) || ')';
				FOR tempK IN SELECT connectionid AS userid FROM connection_usr WHERE userid = tempJ.userid AND connectionid <> tempI.userid AND status = 'Accept' UNION ALL SELECT userid FROM connection_usr WHERE connectionid = tempJ.userid AND userid <> tempI.userid AND status = 'Accept' LOOP
					EXECUTE 'INSERT INTO '|| quote_ident(tableName) ||' ' || 'VALUES (' || quote_literal(tempK.userid) || ')';	
				END LOOP;
			END LOOP;
	END LOOP;
END
$$ LANGUAGE plpgsql;	
