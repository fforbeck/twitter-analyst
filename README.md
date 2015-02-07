### App Design

![Tweet Analyst System](/documents/tweetanalystsystem.png)


---

### Installing on Ubuntu 13.10

1 - Install the JDK7+

    sudo apt-get install openjdk-7-jdk

2 - Install your VerticaDB with Examples (I am using the public schema name)

3 - Play Framework Activator

3.1 - Download the zip: 

    wget http://downloads.typesafe.com/typesafe-activator/1.2.12/typesafe-activator-1.2.12-minimal.zip

3.2 - Extract the ZIP file in your home dir

3.3 - Export the activator path into .bash_profile if Ubuntu OS.

    export PATH=$PATH:/relativePath/to/activator
    
3.4 - To reload the file execute:

    source .bash_profile
    
3.5 - To check if activator is ready, run:

    activator -help
    
It will download some libraries before show the help options.

4 - Install Redis

4.1 - Download the stable version

    wget http://download.redis.io/releases/redis-2.8.19.tar.gz 
    
4.2 - Extract the file into your home dir


4.3 - Run:

    sudo make install
    cd src
    make test
    
4.4 - To start the server, run:

    redis-server
    
4.5 - To Connect to the server with a redis client, in another terminal tab, execute:

    redis-cli
    
once it`s connected, send the command:

    PING
    
The response should be 

    PONG
    
it means the server is up and running.

5 - Creating the Model and loading the Data

5.1 - To create the database model in VerticaDB execute:
    
    ddl/1.sql
    
5.2 - To load about 10K+ tweets with $HPQ tag into your VerticaBD. Make sure you had place your schema name as 'public'.

    dml/2.sql 
    
6 - Starting the Application

6.1 - Put your redis server up

6.2 - Put your VerticaDB up

6.3 - Extract the twitter-analyst.zip

6.4 - Edit the twitter-analyst/conf/application.conf file:

    # VERTICA DB
    db.default.url="jdbc:vertica://<server-ip-address>:5433/VMart"
    db.default.user=dbadmin
    db.default.password=""
    db.default.schema="public"
    db.default.jndiName=DefaultDS
		
	# Redis
	redis.host="<server-ip-address>"
	
6.5 - Setup your Twitter keys in the same file:
    
    # Twitter 4J OAuth
    twitter.oauth.consumerKey=""
    twitter.oauth.consumerSecret=""
    twitter.oauth.accessToken=""
    twitter.oauth.accessTokenSecret=""
6.6 - Setup you HP IDOL API key
    
    # HP IDOL Platform
    hp.idol.analyze.sentiment.uri="https://api.idolondemand.com/1/api/sync/analyzesentiment/v1?text=%TWEET%&language=%LANG%&apikey=<your-api-key>"

6.5 - Go to the root folder 

    twitter-analyst/

6.6 - Make sure you OS clock is in sync

6.7 - Execute:

    activator run
    
It will download all the necessary dependencies for the project and play framework. Takes a lot of time in the first time you execute it.

6.6 - Open the browser at 
    
    localhost:9000
    
6.6.1 You will see the live tweets chart. If you don`t see any red/green dots in this chart just use your own tweet account to tweet using the $HPQ tag with negative/positive sentiment. You will receive the analysis in real time in this chart. Just place the mouse over the dot to see the date, score, user and tweet content.

6.6.2 The time line tweets chart loads all tweets from DB and split them in 2 series: positive, negative. You can see when it happened and to get more details just reduce the bottom bar range, the zoom will be applied and you can put the mouse over the dot to see the tweet details.

6.6.4 The pie chart just show all tweets in DB with $HPQ tag in 3 categories: Positive, Negative and Neutral.

---

### License

This software is licensed under the Apache 2 license, quoted below.

Copyright 2015 Felipe Forbeck (http://www.felipeforbeck.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

---
