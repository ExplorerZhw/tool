
  CREATE TABLE "JC_SITE" 
   (	"SITE_ID" VARCHAR2(32 CHAR) NOT NULL ENABLE, 
	"CREATE_BY" VARCHAR2(32 CHAR), 
	"CREATE_DATETIME" DATE, 
	"MEMO" VARCHAR2(255 CHAR), 
	"SITE_NAME" VARCHAR2(100 CHAR), 
	"SITE_NO" VARCHAR2(30 CHAR), 
	"SITE_TYPE" VARCHAR2(200), 
	"STATUS" VARCHAR2(10 CHAR), 
	"UPDATE_BY" VARCHAR2(32 CHAR), 
	"UPDATE_DATETIME" DATE, 
	 PRIMARY KEY ("SITE_ID")
  ) 
 