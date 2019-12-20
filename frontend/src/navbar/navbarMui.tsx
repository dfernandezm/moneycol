import React, {useEffect, useState} from 'react';
import { fade, makeStyles, Theme, createStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import MenuItem from '@material-ui/core/MenuItem';
import Menu from '@material-ui/core/Menu';
import MenuIcon from '@material-ui/icons/Menu';
import AccountCircle from '@material-ui/icons/AccountCircle';
import MoreIcon from '@material-ui/icons/MoreVert';
import LockOpenRounded from '@material-ui/icons/LockOpenRounded';
import PermMediaRounded from '@material-ui/icons/PermMediaRounded';

import {
    NavLink
} from "react-router-dom";

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
    }),
);

interface Measures {
    top: number,
    height: number,
    scroll: number
}

type El = HTMLElement | null;

const NavBarMui: React.FC = () => {
    const classes = useStyles();
    
    const [measures, setMeasures] = useState<Measures>({ top: 0, height: 0, scroll: -1 });
    const [initLoad, setInitLoad] = useState<boolean>(true);

    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const [mobileMoreAnchorEl, setMobileMoreAnchorEl] = useState<null | HTMLElement>(null);

    const isMenuOpen = Boolean(anchorEl);
    const isMobileMenuOpen = Boolean(mobileMoreAnchorEl);

    useEffect(() => {
        if (initLoad) {
            const el: El = document.querySelector('nav');
            if (el != null) {
                setMeasures({ top: el.offsetTop, height: el.offsetHeight, scroll: -1 });
                setInitLoad(false);
                window.addEventListener('scroll', handleScroll);
            }
        } else {
            measures.scroll > measures.top ?
                document.body.style.paddingTop = `${measures.height}px` :
                document.body.style.paddingTop = '0px';
        }
    });

    const handleScroll = () => {
        setMeasures({ ...measures, scroll: window.scrollY });
    }

    const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
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
            <MenuItem onClick={handleMenuClose}>Profile</MenuItem>
            <MenuItem onClick={handleMenuClose}>My account</MenuItem>
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
            <MenuItem>
                <IconButton aria-label="My Collections" color="inherit" />
                <p>My Collections</p>
            </MenuItem>
            <MenuItem>
                <IconButton aria-label="Sign in" color="inherit" />
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
                        edge="start"
                        className={classes.menuButton}
                        color="inherit"
                        aria-label="open drawer">
                        <MenuIcon />
                    </IconButton>
                    <Typography className={classes.title} variant="h6" noWrap>MoneyCol</Typography>

                    <SearchInTopBar />

                    <div className={classes.grow} />

                    <div className={classes.sectionDesktop}>
                       
                            <IconButton
                                edge="end"
                                aria-label="collections"
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
                            <LockOpenRounded />
                        </IconButton>
                        <IconButton
                            edge="end"
                            aria-label="account of current user"
                            aria-controls={menuId}
                            aria-haspopup="true"
                            onClick={handleProfileMenuOpen}
                            color="inherit"
                        >
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
           
        </div>
    );
}

export { NavBarMui };