import React from 'react';
import firebaseRepo from "./firestore/firebaseRepository";

class User extends React.Component {
    constructor() {
        super();
        this.state = {
         username: "",   
         email: "",
         firstname: "",
         lastname: ""
        };
    }

    // If we use methods instead of arrow function we would have to bind to 'this' in the constructor
    updateInput = e => {
        this.setState({
          [e.target.name]: e.target.value
        });
      }

    addUser = e => {
        e.preventDefault();
        
        firebaseRepo.addUser(this.state);

        this.setState({
            username: "",   
            email: "",
            firstname: "",
            lastname: ""
        });
    }
    
  render() {

    const registerForm = (
        <div class="container">
            <div class="row">
                <form class="col s12" id="reg-form" onSubmit={this.addUser}>
                    <div class="row">
                        <div class="input-field col s6">
                            <input id="first_name" type="text" className="validate" 
                                onChange={this.updateInput}
                                name="firstname"
                                value={this.state.firstname} required />
                            <label htmlFor="first_name">First Name</label>
                            
                        </div>
                        <div class="input-field col s6">
                            <input id="last_name" type="text"
                                name="lastname" 
                                onChange={this.updateInput}
                                value={this.state.lastname}
                                className="validate" required />
                            <label htmlFor="last_name">Last Name</label>
                        </div>
                    </div>
                    <div class="row">
                        <div class="input-field col s6">
                            <input id="username" type="text" 
                                name="username"
                                className="validate" 
                                onChange={this.updateInput}
                                value={this.state.username}
                                required />
                            <label htmlFor="username">Username</label>
                        </div>
                        <div class="input-field col s6">
                            <input id="email" type="email"
                                name="email" 
                                className="validate"
                                onChange={this.updateInput}
                                value={this.state.email}
                                required />
                            <label htmlFor="email">Email</label>
                        </div>
                    </div>
                    <div class="input-field col s12 right-align">
                        <button class="btn btn-large btn-register waves-effect waves-light" type="submit">Register
                         <i class="material-icons right">done</i>
                        </button>
                    </div>
                </form>
            </div>
    </div>
    );
    return registerForm;
    }
}
export default User;