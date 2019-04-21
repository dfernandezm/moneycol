import React from 'react';
import userApi from "./apiCalls/userApi";

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
        
        userApi.addUser(this.state);

        this.setState({
            username: "",   
            email: "",
            firstname: "",
            lastname: ""
        });
    }
    
  render() {

    const registerForm = (
        <div className="container">
            <div className="row">
                <form className="col s12" id="reg-form" onSubmit={this.addUser}>
                    <div className="row">
                        <div className="input-field col s6">
                            <input id="first_name" type="text" className="validate" 
                                onChange={this.updateInput}
                                name="firstname"
                                value={this.state.firstname} required />
                            <label htmlFor="first_name">First Name</label>
                            
                        </div>
                        <div className="input-field col s6">
                            <input id="last_name" type="text"
                                name="lastname" 
                                onChange={this.updateInput}
                                value={this.state.lastname}
                                className="validate" required />
                            <label htmlFor="last_name">Last Name</label>
                        </div>
                    </div>
                    <div className="row">
                        <div className="input-field col s6">
                            <input id="username" type="text" 
                                name="username"
                                className="validate" 
                                onChange={this.updateInput}
                                value={this.state.username}
                                required />
                            <label htmlFor="username">Username</label>
                        </div>
                        <div className="input-field col s6">
                            <input id="email" type="email"
                                name="email" 
                                className="validate"
                                onChange={this.updateInput}
                                value={this.state.email}
                                required />
                            <label htmlFor="email">Email</label>
                        </div>
                    </div>
                    <div className="input-field col s12 right-align">
                        <button className="btn btn-large btn-register waves-effect waves-light" type="submit">Register
                         <i className="material-icons right">done</i>
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