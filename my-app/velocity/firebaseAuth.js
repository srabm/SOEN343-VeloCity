import { getAuth, signInWithEmailAndPassword, createUserWithEmailAndPassword } from "firebase/auth";
import app from './firebase-config.js'

const auth = getAuth(app);

document.addEventListener('DOMContentLoaded', function() {
    console.log("Clicked");
    const signUp=document.getElementById("signup");
    signUp.addEventListener('click', (e)=>{
    e.preventDefault();
    const email=document.getElementById('email').value;
    const password=document.getElementById('password').value;
    console.log("Clicked");
    console.log(email);
    console.log(password);
    
    createUserWithEmailAndPassword(auth, email, password)
    .then((userCredential) => {
        //Succesful registration
        const user = userCredential.user;
    })
    .catch((error) => {
        const errorCode = error.code;
        const errorMessage = error.message;
    });
})
});



// signInWithEmailAndPassword(auth, email, password)
//     .then((userCredential) => {
//         //Successful login
//         const user = userCredential.user;
//     })
//     .catch((error) => {
//         const errorCode = error.code;
//         const errorMessage = error.message;
//     });


export default auth;