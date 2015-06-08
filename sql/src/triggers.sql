/* creates new msgID when user sends message */
CREATE SEQUENCE msgIDSequence;
SELECT setval('msgIDSequence', 27811);

/* searches for valid connection possibilities */
CREATE OR REPLACE FUNCTION conn_search(connName varchar) RETURNS VOID AS $$
DECLARE
	tempResult connection_usr%rowtype;
	tempI connection_usr%rowtype;
	tempJ connection_usr%rowtype;
	tableName ALIAS FOR $1;
	sql text;
BEGIN 
	SELECT * INTO tempResult 
	FROM pg_catalog.pg_tables
	WHERE tablename = 'connName';
	
	IF NOT FOUND THEN
		BEGIN	
			RAISE WARNING 'before execute table';
			EXECUTE 'CREATE TABLE '|| quote_ident(tableName) ||' (userId varchar(50))';
		EXCEPTION WHEN duplicate_table THEN

		END;
	END IF;
	RAISE DEBUG 'just a test';
	FOR tempI IN SELECT connectionid FROM connection_usr WHERE userid = connName AND status = 'Accept' UNION SELECT userid FROM connection_usr WHERE connectionid = connName AND status = 'Accept' LOOP
			FOR tempJ IN SELECT connectionid FROM connection_usr WHERE userid = tempI.userid AND connectionid <> connName AND status = 'Accept' UNION SELECT userid FROM connection_usr WHERE connectionid = tempI.userid AND userid <> connName AND status = 'Accept' LOOP 
				EXECUTE 'INSERT INTO '|| quote_ident(tableName) ||' ' || 'VALUES (' || quote_literal(tempJ.userid) || ')';
			END LOOP;
		EXECUTE 'INSERT INTO '|| quote_ident(tableName) ||' ' || 'VALUES (' || quote_literal(tempI.userid) || ')';
	END LOOP;
END
$$ LANGUAGE plpgsql;	
