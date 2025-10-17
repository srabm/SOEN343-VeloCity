import { getAuth, signInWithEmailAndPassword, createUserWithEmailAndPassword, sendPasswordResetEmail, signOut } from "firebase/auth";
import app from './firebase-config.js'

const auth = getAuth(app);

const signUp = (email, password) => {
    return createUserWithEmailAndPassword(auth, email, password);
}

const login = (email, password) => {
    return signInWithEmailAndPassword(auth, email, password);
}

const resetPassword = (email) => {
    return sendPasswordResetEmail(auth, email);
}

const signOutUser = () => { 
    return signOut(auth);
}

export {signUp, login, resetPassword, signOutUser};