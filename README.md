# SOEN343-VeloCity

<img
    alt="VeloCity"
    src="./my-app/velocity/src/assets/banner.png"
/>

## Contributors
| Name                    | ID        | Role    | GitHub Username     
|-------------------------|-----------|---------|------------|
| Srabanti Mazumdar     | 40263255  | Front End Engineer & Scrum Master | [@srabm](https://github.com/srabm)             |
| Jennifer Nguyen          | 40178603  | Front End Engineer | [@jenniferngu](https://github.com/jenniferngu) | 
| Botao Yang     | 40213554  | Front End Engineer | [@cherryaki1](https://github.com/cherryaki1)             |
| Hendrik Tebeng    | 40282196  | Back End Engineer | [@hendriktebeng](https://github.com/hendriktebeng)       |
| Karan Kuma   | 40277342  | Back End Engineer | [@zekaran30](https://github.com/zekaran30)           |
| Sofia Cimon        | 40282210  | Back End Engineer | [@sofiacimon](https://github.com/sofiacimon)               |

---

## Install Front End Dependencies

1. First make sure you have [Node](https://nodejs.org/en) installed
2. Change your directory from root to `velocity` which is our front end directory
```
cd velocity
```
3. Run the following command to install the front end dependencies:
```
npm install
```
4. Add our `.env` file in the `velocity` directory
---

## How to Get the Front End Running
1. Change your directory from root to `velocity` which is our front end directory
```
cd velocity
```
2. Run the following command to start the front end server:
```
npm run dev
```
3. Open your project at [localhost:5173](http://localhost:5173/)

---

## How to Get the Backend Running
1. Add our `firebase-service-account.json` file under `backend\src\main\resources\`
2. Make sure you have [Java 21](https://www.oracle.com/java/technologies/downloads/) or a later version installed
3. Change your directory from root to `backend` which is our backend directory
```
cd backend
```
4. Run the following command to start the backend server:
```
./gradlew bootRun
```
