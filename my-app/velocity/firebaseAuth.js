import { getAuth, signInWithEmailAndPassword, createUserWithEmailAndPassword } from "firebase/auth";
import app from './firebase-config.js'

const auth = getAuth(app);

const signUp = (email, password) => {
    return createUserWithEmailAndPassword(auth, email, password);
}

export default signUp;