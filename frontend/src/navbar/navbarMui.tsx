import React, { useState } from 'react';
//import React, {useEffect, useState} from 'react';
import { Theme, createStyles, makeStyles } from '@material-ui/core/styles';

//import { Theme, createStyles, fade, makeStyles } from '@material-ui/core/styles';
import AccountCircle from '@material-ui/icons/AccountCircle';
import AppBar from '@material-ui/core/AppBar';
import IconButton from '@material-ui/core/IconButton';
import LockOpenRounded from '@material-ui/icons/LockOpenRounded';
import Menu from '@material-ui/core/Menu';
import MenuIcon from '@material-ui/icons/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import MoreIcon from '@material-ui/icons/MoreVert';
import {
    NavLink
} from "react-router-dom";
import PermMediaRounded from '@material-ui/icons/PermMediaRounded';
import SearchInTopBar from '../search/searchInTopBar'
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        grow: {
            flexGrow: 1,
        },
        appBar: {
            boxShadow: 'none'
        },
        menuButton: {
            marginRight: theme.spacing(2),
        },
        title: {
            display: 'none',
            [theme.breakpoints.up('sm')]: {
                display: 'block',
            },
            fontFamily: "Roboto Mono"
        },
        sectionDesktop: {
            display: 'none',
            [theme.breakpoints.up('md')]: {
                display: 'flex',
            },
        },
        sectionMobile: {
            display: 'flex',
            [theme.breakpoints.up('md')]: {
                display: 'none',
            },
        },
        theNavLink: {
            '&:visited, &:link': {
                textDecoration: 'none',
                color: 'white'
            }
        },
        theNavLinkInverse: {
            '&:visited, &:link': {
                textDecoration: 'none',
                color: 'black'
            }
        },
    }),
);

const NavBarMui: React.FC = () => {
    const classes = useStyles();

    const [mainAnchorEl, setMainAnchorEl] = useState<null | HTMLElement>(null);
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const [mobileMoreAnchorEl, setMobileMoreAnchorEl] = useState<null | HTMLElement>(null);

    const isMainMenuOpen = Boolean(mainAnchorEl)
    const isMenuOpen = Boolean(anchorEl);
    const isMobileMenuOpen = Boolean(mobileMoreAnchorEl);

    const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const handleMainMenuClose = () => {
        setMainAnchorEl(null);
        handleMobileMenuClose();
    };

    const handleMobileMenuClose = () => {
        setMobileMoreAnchorEl(null);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
        handleMobileMenuClose();
    };

    const handleMobileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
        setMobileMoreAnchorEl(event.currentTarget);
    };

    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setMainAnchorEl(event.currentTarget);
    };

    const menuId = 'primary-search-account-menu';
    const renderMenu = (
        <Menu
            anchorEl={anchorEl}
            anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
            id={menuId}
            keepMounted
            transformOrigin={{ vertical: 'top', horizontal: 'right' }}
            open={isMenuOpen}
            onClose={handleMenuClose}>
            <MenuItem component={NavLink} to="/users/updateProfile">
                Profile
            </MenuItem>
            <MenuItem component={NavLink} to="/users/changePassword">
                Change password
            </MenuItem>
        </Menu>
    );

    const mainMenuId = 'primary-main-menu';
    const renderMainMenu = (
        <Menu
            anchorEl={mainAnchorEl}
            anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
            id={mainMenuId}
            transformOrigin={{ vertical: 'top', horizontal: 'right' }}
            open={isMainMenuOpen}
            onClose={handleMainMenuClose}>
            <MenuItem>
                <NavLink exact={true} to="/" className={classes.theNavLinkInverse}> 
                    Home
                </NavLink>
            </MenuItem>
        </Menu>
    );


    const mobileMenuId = 'primary-search-account-menu-mobile';
    const renderMobileMenu = (
        <Menu
            anchorEl={mobileMoreAnchorEl}
            anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
            id={mobileMenuId}
            keepMounted
            transformOrigin={{ vertical: 'top', horizontal: 'right' }}
            open={isMobileMenuOpen}
            onClose={handleMobileMenuClose}>
            <MenuItem component={NavLink} to="/protected">
                <IconButton
                    aria-label="Collections"
                    color="inherit">
                        <PermMediaRounded />
                </IconButton>
                
                    <p>Collections</p>
                
            </MenuItem>
            <MenuItem component={NavLink} to="/login">
                <IconButton aria-label="Sign in" color="inherit">
                    <LockOpenRounded />
                </IconButton>
                <p>Sign in</p>
            </MenuItem>
            <MenuItem onClick={handleProfileMenuOpen}>
                <IconButton
                    aria-label="account of current user"
                    aria-controls="primary-search-account-menu"
                    aria-haspopup="true"
                    color="inherit">
                    <AccountCircle />
                </IconButton>
                <p>Profile</p>
            </MenuItem>
        </Menu>
    );

    return (

        <div className={classes.grow}>

            <AppBar className={classes.appBar} position="fixed" >

                <Toolbar>
                    <IconButton
                        onClick={handleClick}
                        edge="start"
                        className={classes.menuButton}
                        color="inherit"
                        aria-label="open drawer">
                        <MenuIcon />
                    </IconButton>
                    
                    <NavLink exact={true} to="/" className={classes.theNavLink}> 
                        <Typography className={classes.title} variant="h6" noWrap>
                            MoneyCol
                        </Typography>
                    </NavLink>
                    
                    <SearchInTopBar />

                    <div className={classes.grow} />

                    <div className={classes.sectionDesktop}>
                       
                            <IconButton
                                edge="end"
                                aria-label="protected"
                                aria-controls={menuId}
                                color="inherit">
                                  <NavLink exact={true} to="/protected" className={classes.theNavLink}>      
                                    <PermMediaRounded />
                                  </NavLink>
                                
                            </IconButton>
                       
                        <IconButton
                            edge="end"
                            aria-label="signin"
                            aria-controls={menuId}
                            color="inherit">
                            <NavLink exact={true} to="/login" className={classes.theNavLink}>
                                <LockOpenRounded />
                            </NavLink>
                        </IconButton>
                        <IconButton
                                edge="end"
                                aria-label="collections"
                                aria-controls={menuId}
                                color="inherit">
                                  <NavLink exact={true} to="/collections" className={classes.theNavLink}>      
                                    <PermMediaRounded />
                                  </NavLink>
                                
                            </IconButton>
                        <IconButton
                            edge="end"
                            aria-label="account of current user"
                            aria-controls={menuId}
                            aria-haspopup="true"
                            onClick={handleProfileMenuOpen}
                            color="inherit" >
                        <AccountCircle />
                        </IconButton>
                    </div>
                    <div className={classes.sectionMobile}>
                        <IconButton
                            aria-label="show more"
                            aria-controls={mobileMenuId}
                            aria-haspopup="true"
                            onClick={handleMobileMenuOpen}
                            color="inherit">
                            <MoreIcon />
                        </IconButton>
                    </div>
                </Toolbar>
            </AppBar>
            {renderMobileMenu}
            {renderMenu}
            {renderMainMenu}
           
        </div>
    );
}

export { NavBarMui };